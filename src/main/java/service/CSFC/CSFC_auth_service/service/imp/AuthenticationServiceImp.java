package service.CSFC.CSFC_auth_service.service.imp;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import service.CSFC.CSFC_auth_service.common.exception.BadRequestException;
import service.CSFC.CSFC_auth_service.common.exception.ResourceNotFoundException;
import service.CSFC.CSFC_auth_service.common.exception.UnauthorizedException;
import service.CSFC.CSFC_auth_service.common.security.CustomerUserDetails;
import service.CSFC.CSFC_auth_service.mapper.UserMapper;
import service.CSFC.CSFC_auth_service.model.dto.request.*;
import service.CSFC.CSFC_auth_service.model.dto.response.AuthResponse;
import service.CSFC.CSFC_auth_service.model.dto.response.RegisterResponse;
import service.CSFC.CSFC_auth_service.model.entity.Roles;
import service.CSFC.CSFC_auth_service.model.entity.Users;
import service.CSFC.CSFC_auth_service.repository.RolesRepository;
import service.CSFC.CSFC_auth_service.repository.UsersRepository;
import service.CSFC.CSFC_auth_service.service.AuthenticationService;
import service.CSFC.CSFC_auth_service.service.JwtService;

import java.util.Date;
import java.util.concurrent.TimeUnit;
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImp implements AuthenticationService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String USERNAME_TOKEN_PREFIX = "username_token:";
    private static final String BLACKLIST_PREFIX = "jwt_blacklist:";

    private final UsersRepository usersRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final RolesRepository rolesRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.access-token-expiration}")
    private Long accessExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshExpiration;

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        Users user = usersRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + request.getEmail()));

        CustomerUserDetails userDetails = new CustomerUserDetails(user);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // 1. Lưu vào Database
        user.setRefreshToken(refreshToken);
        usersRepository.save(user);

        // 2. Lưu vào Redis để quản lý session
        saveRefreshToken(user.getEmail(), refreshToken, refreshExpiration);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String oldRefreshToken = request.getRefreshToken();

        // 1. Trích xuất email từ token cũ
        String email = jwtService.extractUsername(oldRefreshToken);
        if (email == null) {
            throw new UnauthorizedException("RefreshToken không hợp lệ");
        }

        // 2. Kiểm tra tính tồn tại trong Redis
        if (!validateRefreshToken(oldRefreshToken)) {
            throw new UnauthorizedException("RefreshToken đã hết hạn hoặc đã bị đăng xuất");
        }

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));

        // 3. Kiểm tra token cũ có khớp với token trong DB không (Tăng cường bảo mật)
        if (!oldRefreshToken.equals(user.getRefreshToken())) {
            deleteRefreshToken(oldRefreshToken); // Xóa luôn token nghi vấn trên Redis
            throw new UnauthorizedException("Phát hiện truy cập trái phép bằng Token cũ");
        }

        CustomerUserDetails customUserDetails = new CustomerUserDetails(user);
        String newAccessToken = jwtService.generateAccessToken(customUserDetails);
        String newRefreshToken = jwtService.generateRefreshToken(customUserDetails);

        // 4. Xóa token cũ và lưu token mới vào Redis (Token Rotation)
        deleteRefreshToken(oldRefreshToken);
        saveRefreshToken(email, newRefreshToken, refreshExpiration);

        // 5. Cập nhật Database
        user.setRefreshToken(newRefreshToken);
        usersRepository.save(user);

        // 6. Trả về Token MỚI (Lưu ý: newRefreshToken)
        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    @Override
    public void saveRefreshToken(String username, String refreshToken, long expirationMillis) {
        // Lưu 2 chiều để dễ dàng tìm kiếm theo token hoặc theo username khi logout/revoke
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(key, username, expirationMillis, TimeUnit.MILLISECONDS);

        String usernameKey = USERNAME_TOKEN_PREFIX + username;
        redisTemplate.opsForValue().set(usernameKey, refreshToken, expirationMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean validateRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void deleteRefreshToken(String refreshToken) {
        // Tìm username dựa trên token để xóa cả key username
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        Object username = redisTemplate.opsForValue().get(key);
        if (username != null) {
            redisTemplate.delete(USERNAME_TOKEN_PREFIX + username.toString());
        }
        redisTemplate.delete(key);
    }

    @Override
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            return;
        }

        try {
            // 1. Blacklist Access Token
            Date expirationDate = jwtService.extractExpiration(token);
            long remainingTime = expirationDate.getTime() - System.currentTimeMillis();

            if (remainingTime > 0) {
                redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "REVOKED", remainingTime, TimeUnit.MILLISECONDS);
            }

            // 2. Xóa Refresh Token liên quan
            String email = jwtService.extractUsername(token);
            if (email != null) {
                deleteRefreshTokenByUsername(email);
                // Xóa token trong DB
                usersRepository.findByEmail(email).ifPresent(u -> {
                    u.setRefreshToken(null);
                    usersRepository.save(u);
                });
            }
        } catch (Exception e) {
            throw new UnauthorizedException("Logout thất bại: Token không hợp lệ");
        }
    }

    @Override
    public void deleteRefreshTokenByUsername(String username) {
        String usernameKey = USERNAME_TOKEN_PREFIX + username;
        Object refreshTokenObj = redisTemplate.opsForValue().get(usernameKey);
        if (refreshTokenObj != null) {
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + refreshTokenObj.toString());
        }
        redisTemplate.delete(usernameKey);
    }

    private AuthResponse buildAuthResponse(Users user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(accessExpiration) // Dùng biến config thay vì hardcode
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (usersRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email này đã tồn tại");
        }
        Roles customerRole = rolesRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Role CUSTOMER không tồn tại"));

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Users user = userMapper.toEntity(request, encodedPassword);
        user.setRole(customerRole);
        usersRepository.save(user);

        return RegisterResponse.builder()
                .user(userMapper.toResponse(user))
                .build();
    }
}
