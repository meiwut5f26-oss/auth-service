package service.CSFC.CSFC_auth_service.service;

import service.CSFC.CSFC_auth_service.model.dto.request.ForgotPasswordRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.ResetPasswordRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.VerifyOtpRequest;

public interface PasswordService {
    void requestForgotPassword(ForgotPasswordRequest request);
    String verifyOtp(VerifyOtpRequest request);
    void resetPassword(ResetPasswordRequest request);
}
