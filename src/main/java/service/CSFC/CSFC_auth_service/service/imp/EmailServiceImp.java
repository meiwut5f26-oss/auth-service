package service.CSFC.CSFC_auth_service.service.imp;

import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.emails.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import service.CSFC.CSFC_auth_service.common.exception.BusinessException;
import service.CSFC.CSFC_auth_service.service.EmailService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImp implements EmailService {

    @Value("${mailersend.api-key}")
    private String apiKey;

    @Value("${mailersend.from}")
    private String fromEmail;

    @Value("${mailersend.from-name:CSFC Auth Service}")
    private String fromName;

    @Override
    public void sendEmail(String toEmail, String otp) {
        validateConfig();

        MailerSend mailerSend = new MailerSend();
        mailerSend.setToken(apiKey);

        String subject = "Mã xác nhận quên mật khẩu";
        String htmlBody = buildHtmlBody(otp);

        Email email = new Email();
        email.setFrom(fromEmail, fromName);
        email.addRecipient(toEmail, null);
        email.setSubject(subject);
        email.setHtml(htmlBody);

        try {
            mailerSend.emails().send(email);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}", toEmail, e);
            throw new BusinessException("Không thể gửi mã xác thực. Vui lòng thử lại sau.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateConfig() {
        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(fromEmail)) {
            throw new BusinessException("Cấu hình gửi email chưa đầy đủ.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String buildHtmlBody(String otp) {
        return "<p>Xin chào,</p>" +
                "<p>Bạn đã yêu cầu đặt lại mật khẩu. Mã OTP của bạn là: <strong>" + otp + "</strong></p>" +
                "<p>Mã có hiệu lực trong 5 phút. Nếu bạn không thực hiện yêu cầu này, hãy bỏ qua email.</p>" +
                "<p>Trân trọng,<br/>CSFC Auth Service</p>";
    }
}
