package service.CSFC.CSFC_auth_service.service.imp;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import service.CSFC.CSFC_auth_service.service.EmailService;

@Service
@RequiredArgsConstructor
public class EmailServiceImp implements EmailService {

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${resend.from-email}")
    private String fromEmail;

    @Async
    @Override
    public void sendEmail(String toEmail, String otp) {
        try {
            Resend resend = new Resend(resendApiKey);

            // Đổi SendEmailRequest thành CreateEmailOptions
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(toEmail)
                    .subject("Mã xác nhận OTP - CSFC")
                    .html("<strong>Mã OTP của bạn là: " + otp + "</strong><br>Mã có hiệu lực trong 5 phút.")
                    .build();

            resend.emails().send(params);
        } catch (Exception e) {
            System.err.println("Lỗi Resend API: " + e.getMessage());
        }
    }
}