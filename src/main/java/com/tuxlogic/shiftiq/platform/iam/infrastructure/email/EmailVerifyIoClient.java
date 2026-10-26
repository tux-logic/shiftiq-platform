package com.tuxlogic.shiftiq.platform.iam.infrastructure.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.email.EmailVerificationResult;
import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.email.EmailVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;

/**
 * Infrastructure client implementation of EmailVerificationService connecting to EmailVerify.io REST API.
 */
@Service
public class EmailVerifyIoClient implements EmailVerificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailVerifyIoClient.class);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    private final String apiUrl;
    private final String apiKey;
    private final RestClient restClient;

    @Autowired
    public EmailVerifyIoClient(
            @Value("${emailverify.api.url:https://app.emailverify.io/api/v1/validate}") String apiUrl,
            @Value("${emailverify.api.key:}") String apiKey
    ) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public EmailVerifyIoClient(String apiUrl, String apiKey, RestClient restClient) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.restClient = restClient;
    }

    @Override
    public EmailVerificationResult verifyEmail(String email) {
        if (email == null || email.isBlank()) {
            return EmailVerificationResult.ofInvalid("invalid", "empty", "Email address is null or empty");
        }

        if (apiKey == null || apiKey.isBlank()) {
            LOGGER.warn("EmailVerify API key is not configured. Falling back to permit email: {}", email);
            return EmailVerificationResult.ofValid("unconfigured", "bypassed");
        }

        try {
            URI uri = UriComponentsBuilder.fromUriString(apiUrl)
                    .queryParam("key", apiKey)
                    .queryParam("email", email)
                    .build()
                    .toUri();

            EmailVerifyResponse response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(EmailVerifyResponse.class);

            if (response == null) {
                LOGGER.warn("EmailVerify.io returned empty response for email {}", email);
                return EmailVerificationResult.ofInvalid("unknown", "no_response", "Empty response from verification provider");
            }

            if (response.error() != null && !response.error().isBlank()) {
                LOGGER.error("EmailVerify.io API returned error for email {}: {}", email, response.error());
                return EmailVerificationResult.ofInvalid("error", "api_error", response.error());
            }

            String status = response.status();
            String subStatus = response.subStatus();

            LOGGER.info("EmailVerify.io validation for {}: status={}, sub_status={}", email, status, subStatus);

            if (status != null && "valid".equalsIgnoreCase(status.trim())) {
                return EmailVerificationResult.ofValid(status, subStatus);
            }

            return EmailVerificationResult.ofInvalid(
                    status != null ? status : "invalid",
                    subStatus,
                    "Email verification status is " + status
            );

        } catch (Exception e) {
            LOGGER.error("Failed to verify email {} with EmailVerify.io: {}", email, e.getMessage());
            return EmailVerificationResult.ofInvalid("error", "communication_failure", e.getMessage());
        }
    }

    public record EmailVerifyResponse(
            String email,
            String status,
            @JsonProperty("sub_status") String subStatus,
            String error
    ) {}
}
