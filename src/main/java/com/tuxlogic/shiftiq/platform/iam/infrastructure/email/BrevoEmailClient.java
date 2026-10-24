package com.tuxlogic.shiftiq.platform.iam.infrastructure.email;

import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.email.TransactionalEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

/**
 * Infrastructure client implementation of TransactionalEmailService connecting to Brevo API v3.
 */
@Service
public class BrevoEmailClient implements TransactionalEmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrevoEmailClient.class);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    private final String apiUrl;
    private final String apiKey;
    private final String senderEmail;
    private final String senderName;
    private final RestClient restClient;

    public BrevoEmailClient(
            @Value("${brevo.api.url:https://api.brevo.com/v3/smtp/email}") String apiUrl,
            @Value("${brevo.api.key:}") String apiKey,
            @Value("${brevo.sender.email:no-reply@shiftiq.com}") String senderEmail,
            @Value("${brevo.sender.name:ShiftIQ Platform}") String senderName,
            RestClient.Builder restClientBuilder
    ) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        this.restClient = restClientBuilder
                .requestFactory(requestFactory)
                .build();
    }

    public BrevoEmailClient(String apiUrl, String apiKey, String senderEmail, String senderName, RestClient restClient) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.restClient = restClient;
    }

    @Override
    public boolean sendEmail(String toEmail, String subject, String htmlContent) {
        if (toEmail == null || toEmail.isBlank()) {
            LOGGER.warn("Cannot send email: recipient address is null or empty");
            return false;
        }

        if (apiKey == null || apiKey.isBlank()) {
            LOGGER.warn("Brevo API key is not configured. Simulating email dispatch to {}", toEmail);
            return true;
        }

        try {
            var request = new BrevoSendEmailRequest(
                    new BrevoParticipant(senderName, senderEmail),
                    List.of(new BrevoRecipient(toEmail)),
                    subject,
                    htmlContent
            );

            var response = restClient.post()
                    .uri(apiUrl)
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(BrevoSendEmailResponse.class);

            if (response != null && response.messageId() != null) {
                LOGGER.info("Transactional email sent via Brevo to {}. Message ID: {}", toEmail, response.messageId());
                return true;
            }

            LOGGER.warn("Brevo API call completed but messageId was empty for {}", toEmail);
            return false;

        } catch (Exception e) {
            LOGGER.error("Failed to send transactional email via Brevo to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    public record BrevoParticipant(String name, String email) {}
    public record BrevoRecipient(String email) {}
    public record BrevoSendEmailRequest(
            BrevoParticipant sender,
            List<BrevoRecipient> to,
            String subject,
            String htmlContent
    ) {}
    public record BrevoSendEmailResponse(String messageId) {}
}
