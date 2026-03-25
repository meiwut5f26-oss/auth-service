package service.CSFC.CSFC_auth_service.service.imp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import service.CSFC.CSFC_auth_service.service.EmailService;

import java.util.List;

@Service
@Slf4j
public class EmailServiceImp implements EmailService {

    private static final String RESEND_PATH = "/emails";

    private final RestClient restClient;
    private final String fromEmail;
    private final String fromName;
    private final String apiKey;

    public EmailServiceImp(
            RestClient.Builder restClientBuilder,
            @Value("${resend.api-key:}") String apiKey,
            @Value("${resend.from-email:onboarding@resend.dev}") String fromEmail,
            @Value("${mail.from-name:CSFC Auth Service}") String fromName,
            @Value("${resend.base-url:https://api.resend.com}") String baseUrl) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Async
    @Override
    public void sendEmail(String toEmail, String otp) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Resend API key is missing; skipping OTP email to {}", toEmail);
            return;
        }

        ResendEmailRequest payload = new ResendEmailRequest(
                formatFromField(),
                List.of(toEmail),
                "Mã xác nhận OTP - CSFC",
                "<strong>Mã OTP của bạn là: " + otp + "</strong><br>Mã có hiệu lực trong 5 phút."
        );

        try {
            restClient.post()
                    .uri(RESEND_PATH)
                    .body(payload)
                    .retrieve()
                    .body(ResendEmailResponse.class);
        } catch (RestClientException ex) {
            log.error("Gửi email OTP thất bại tới {}: {}", toEmail, ex.getMessage(), ex);
        }
    }

    private String formatFromField() {
        return String.format("%s <%s>", fromName, fromEmail);
    }

    private record ResendEmailRequest(String from, List<String> to, String subject, String html) {
    }

    private record ResendEmailResponse(String id) {
    }
}