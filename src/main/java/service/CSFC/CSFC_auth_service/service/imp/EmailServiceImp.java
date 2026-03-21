package service.CSFC.CSFC_auth_service.service.imp;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import service.CSFC.CSFC_auth_service.service.EmailService;

@Service
@RequiredArgsConstructor
public class EmailServiceImp implements EmailService {

    private final JavaMailSender mailSender;

    @Async //  Giúp gửi mail ở luồng ngầm, không block API
    @Override
    public void sendEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Mã xác nhận quên mật khẩu - Hệ thống Quản lý Bồi thường");
            message.setText("Xin chào,\n\n" +
                    "Bạn đã yêu cầu đặt lại mật khẩu. Mã OTP của bạn là: " + otp + "\n\n" +
                    "Mã này có hiệu lực trong vòng 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\n" +
                    "Trân trọng,\nBan Quản trị Hệ thống.");

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email đến " + toEmail + ": " + e.getMessage());
        }
    }
}
