package service.CSFC.CSFC_auth_service.service.imp;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import service.CSFC.CSFC_auth_service.service.EmailService;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImp implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String fromEmail;

    @Value("${mail.from-name:CSFC Auth Service}")
    private String fromName;

    @Async
    @Override
    public void sendEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
            helper.setTo(toEmail);
            helper.setFrom(new InternetAddress(fromEmail, fromName));
            helper.setSubject("Mã xác nhận OTP - CSFC");
            helper.setText("<strong>Mã OTP của bạn là: " + otp + "</strong><br>Mã có hiệu lực trong 5 phút.", true);
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException ex) {
            log.error("Gửi email OTP thất bại tới {}: {}", toEmail, ex.getMessage(), ex);
        }
    }
}