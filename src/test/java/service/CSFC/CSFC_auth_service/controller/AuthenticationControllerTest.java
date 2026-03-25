package service.CSFC.CSFC_auth_service.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import service.CSFC.CSFC_auth_service.common.response.BaseResponse;
import service.CSFC.CSFC_auth_service.model.constants.CustomerStatus;
import service.CSFC.CSFC_auth_service.model.dto.request.LoginRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.RefreshTokenRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.RegisterRequest;
import service.CSFC.CSFC_auth_service.model.dto.response.AuthResponse;
import service.CSFC.CSFC_auth_service.model.dto.response.RegisterResponse;
import service.CSFC.CSFC_auth_service.model.dto.response.UserResponse;
import service.CSFC.CSFC_auth_service.service.AuthenticationService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    AuthenticationService authenticationService;

    @InjectMocks
    AuthenticationController controller;

    @Test
    void register_ShouldReturnCreatedResponse() {
        RegisterRequest request = new RegisterRequest("Nguyen Van A", "user@example.com", "ChangeMe@123", null, null, UUID.randomUUID());
        RegisterResponse response = RegisterResponse.builder()
                .user(UserResponse.builder()
                        .id(UUID.randomUUID())
                        .email("user@example.com")
                        .name("Nguyen Van A")
                        .status(CustomerStatus.ACTIVE)
                        .build())
                .build();

        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(response);

        ResponseEntity<BaseResponse<RegisterResponse>> result = controller.register(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getUser().getEmail()).isEqualTo("user@example.com");
    }

    @Test
    void login_ShouldReturnAuthTokens() {
        LoginRequest request = new LoginRequest("user@example.com", "ChangeMe@123");
        AuthResponse response = AuthResponse.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .expiresIn(3600L)
                .user(UserResponse.builder().email("user@example.com").build())
                .build();

        when(authenticationService.login(any(LoginRequest.class))).thenReturn(response);

        ResponseEntity<BaseResponse<AuthResponse>> result = controller.login(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getAccessToken()).isEqualTo("access");
    }

    @Test
    void refreshToken_ShouldReturnNewTokens() {
        RefreshTokenRequest request = new RefreshTokenRequest("refreshTokenValue" + "x".repeat(20));
        AuthResponse response = AuthResponse.builder()
                .accessToken("newAccess")
                .refreshToken("newRefresh")
                .expiresIn(3600L)
                .build();

        when(authenticationService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(response);

        ResponseEntity<BaseResponse<AuthResponse>> result = controller.refreshToken(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getRefreshToken()).isEqualTo("newRefresh");
    }

    @Test
    void logout_ShouldInvokeService() {
        String token = "Bearer test-token";

        ResponseEntity<BaseResponse<Void>> response = controller.logout(token);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(authenticationService).logout(eq(token));
    }
}
