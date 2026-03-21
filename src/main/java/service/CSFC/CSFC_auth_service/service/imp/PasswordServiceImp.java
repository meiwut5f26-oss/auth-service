package service.CSFC.CSFC_auth_service.service.imp;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import service.CSFC.CSFC_auth_service.common.exception.BusinessException;
import service.CSFC.CSFC_auth_service.model.dto.request.ForgotPasswordRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.ResetPasswordRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.VerifyOtpRequest;
import service.CSFC.CSFC_auth_service.model.entity.Users;
import service.CSFC.CSFC_auth_service.repository.UsersRepository;
import service.CSFC.CSFC_auth_service.service.AuthenticationService;
import service.CSFC.CSFC_auth_service.service.EmailService;
import service.CSFC.CSFC_auth_service.service.PasswordService;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PasswordServiceImp  implements PasswordService {
    private final UsersRepository usersRepository;
    private final EmailService emailService;
    private final AuthenticationService authService; // Dùng để xóa token đăng nhập cũ
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String OTP_KEY_PREFIX = "pwd_otp:";
    private static final String OTP_COOLDOWN_PREFIX = "pwd_cooldown:";
    private static final String RESET_TOKEN_PREFIX = "pwd_reset_token:";

    private static final long OTP_TTL_MINUTES = 5;
    private static final long COOLDOWN_SECONDS = 60;
    private static final long TOKEN_TTL_MINUTES = 15;


    @Override
    public void requestForgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail();

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Email không tồn tại trong hệ thống"));

        String cooldownKey = OTP_COOLDOWN_PREFIX + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            throw new BusinessException("Vui lòng đợi 60 giây trước khi yêu cầu gửi lại mã");
        }

        String otp = String.valueOf(100000 + new SecureRandom().nextInt(900000));

        redisTemplate.opsForValue().set(OTP_KEY_PREFIX + email, otp, OTP_TTL_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(cooldownKey, "LOCKED", COOLDOWN_SECONDS, TimeUnit.SECONDS);

        emailService.sendEmail(email, otp);
    }

    @Override
    public String verifyOtp(VerifyOtpRequest request) {
        String email = request.getEmail();
        String otpKey = OTP_KEY_PREFIX + email;

        Object savedOtp = redisTemplate.opsForValue().get(otpKey);
        if (savedOtp == null || !savedOtp.toString().equals(request.getOtp())) {
            throw new BusinessException("Mã OTP không chính xác hoặc đã hết hạn");
        }

        redisTemplate.delete(otpKey);
        redisTemplate.delete(OTP_COOLDOWN_PREFIX + email);

        String resetToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(RESET_TOKEN_PREFIX + resetToken, email, TOKEN_TTL_MINUTES, TimeUnit.MINUTES);

        return resetToken;
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        String tokenKey = RESET_TOKEN_PREFIX + request.getResetToken();

        Object emailObj = redisTemplate.opsForValue().get(tokenKey);
        if (emailObj == null) {
            throw new BusinessException("Phiên làm việc đã hết hạn. Vui lòng thực hiện lại từ đầu");
        }

        String email = emailObj.toString();
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usersRepository.save(user);

        redisTemplate.delete(tokenKey);

        authService.deleteRefreshTokenByUsername(user.getName());
    }
}
