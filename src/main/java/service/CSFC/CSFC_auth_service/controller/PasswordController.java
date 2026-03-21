package service.CSFC.CSFC_auth_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import service.CSFC.CSFC_auth_service.common.response.BaseResponse;
import service.CSFC.CSFC_auth_service.model.dto.request.ForgotPasswordRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.ResetPasswordRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.VerifyOtpRequest;
import service.CSFC.CSFC_auth_service.model.dto.response.ApiResponse;
import service.CSFC.CSFC_auth_service.service.PasswordService;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/auth-service/password")
public class PasswordController {

    private final PasswordService passwordService;

    @PostMapping("/forgot")
    @Operation(summary = "Quên mật khẩu", description = "Gửi mã OTP 6 số về email của người dùng")
    public ResponseEntity<BaseResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordService.requestForgotPassword(request);
        return ResponseEntity.ok(BaseResponse.success("Mã xác thực đã được gửi đến email của bạn", null));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Xác nhận OTP", description = "Xác thực mã OTP và nhận token để đổi mật khẩu")
    public ResponseEntity<BaseResponse<Map<String, String>>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        String resetToken = passwordService.verifyOtp(request);
        return ResponseEntity.ok(BaseResponse.success("Xác thực OTP thành công", Map.of("resetToken", resetToken)));
    }

    @PostMapping("/reset")
    @Operation(summary = "Đặt lại mật khẩu", description = "Sử dụng reset token để đặt lại mật khẩu mới")
    public ResponseEntity<BaseResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordService.resetPassword(request);
        return ResponseEntity.ok(BaseResponse.success("Đổi mật khẩu thành công. Vui lòng đăng nhập lại", null));
    }
}

