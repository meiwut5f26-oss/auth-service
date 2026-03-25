package service.CSFC.CSFC_auth_service.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import service.CSFC.CSFC_auth_service.common.response.BaseResponse;
import service.CSFC.CSFC_auth_service.model.dto.request.ForgotPasswordRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.ResetPasswordRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.VerifyOtpRequest;
import service.CSFC.CSFC_auth_service.service.PasswordService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordControllerTest {

    @Mock
    PasswordService passwordService;

    @InjectMocks
    PasswordController controller;

    @Test
    void forgotPassword_ShouldTriggerEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("user@example.com");

        ResponseEntity<BaseResponse<Void>> response = controller.forgotPassword(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(passwordService).requestForgotPassword(any(ForgotPasswordRequest.class));
    }

    @Test
    void verifyOtp_ShouldReturnResetToken() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("user@example.com");
        request.setOtp("123456");

        when(passwordService.verifyOtp(any(VerifyOtpRequest.class))).thenReturn("reset-token");

        ResponseEntity<BaseResponse<java.util.Map<String, String>>> response = controller.verifyOtp(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().get("resetToken")).isEqualTo("reset-token");
        verify(passwordService).verifyOtp(any(VerifyOtpRequest.class));
    }

    @Test
    void resetPassword_ShouldResetSuccessfully() {
        ResetPasswordRequest request = new ResetPasswordRequest("token-token" + "x".repeat(10), "ChangeMe@123");

        ResponseEntity<BaseResponse<Void>> response = controller.resetPassword(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(passwordService).resetPassword(any(ResetPasswordRequest.class));
    }
}
