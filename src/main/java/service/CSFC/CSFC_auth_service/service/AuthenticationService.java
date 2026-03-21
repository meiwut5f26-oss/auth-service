package service.CSFC.CSFC_auth_service.service;

import service.CSFC.CSFC_auth_service.model.dto.request.*;
import service.CSFC.CSFC_auth_service.model.dto.response.AuthResponse;
import service.CSFC.CSFC_auth_service.model.dto.response.RegisterResponse;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationService {

    AuthResponse login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void saveRefreshToken(String username, String refreshToken, long expirationMillis);

    boolean validateRefreshToken(String refreshToken);

    void deleteRefreshToken(String refreshToken);

    void logout(String token);

    void deleteRefreshTokenByUsername(String username);
}