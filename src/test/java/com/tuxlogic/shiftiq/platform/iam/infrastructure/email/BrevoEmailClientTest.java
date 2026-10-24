package com.tuxlogic.shiftiq.platform.iam.infrastructure.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class BrevoEmailClientTest {

    private static final String API_URL = "https://api.brevo.com/v3/smtp/email";
    private static final String API_KEY = "test_brevo_key_123";
    private static final String SENDER_EMAIL = "no-reply@shiftiq.com";
    private static final String SENDER_NAME = "ShiftIQ Platform";

    private MockRestServiceServer mockServer;
    private BrevoEmailClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        client = new BrevoEmailClient(API_URL, API_KEY, SENDER_EMAIL, SENDER_NAME, restClient);
    }

    @Test
    void sendEmail_WhenApiReturns201WithMessageId_ShouldReturnTrue() {
        String jsonResponse = """
                {
                    "messageId": "<20261022.998877@smtp-relay.brevo.com>"
                }
                """;

        mockServer.expect(requestTo(API_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("api-key", API_KEY))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withStatus(HttpStatus.CREATED).body(jsonResponse).contentType(MediaType.APPLICATION_JSON));

        boolean result = client.sendEmail("recipient@example.com", "Test Subject", "<p>Hello</p>");

        assertTrue(result);
        mockServer.verify();
    }

    @Test
    void sendEmail_WhenApiReturnsErrorResponse_ShouldReturnFalse() {
        String jsonError = """
                {
                    "code": "invalid_parameter",
                    "message": "Invalid email address format"
                }
                """;

        mockServer.expect(requestTo(API_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).body(jsonError).contentType(MediaType.APPLICATION_JSON));

        boolean result = client.sendEmail("invalid@example.com", "Subject", "<p>Content</p>");

        assertFalse(result);
        mockServer.verify();
    }

    @Test
    void sendEmail_WhenServerFailsWith500_ShouldReturnFalseGracefully() {
        mockServer.expect(requestTo(API_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        boolean result = client.sendEmail("user@example.com", "Subject", "<p>Content</p>");

        assertFalse(result);
        mockServer.verify();
    }

    @Test
    void sendEmail_WhenRecipientIsNullOrEmpty_ShouldReturnFalseImmediately() {
        assertFalse(client.sendEmail(null, "Subject", "<p>Content</p>"));
        assertFalse(client.sendEmail("   ", "Subject", "<p>Content</p>"));
    }

    @Test
    void sendEmail_WhenApiKeyIsUnconfigured_ShouldSimulateSuccess() {
        BrevoEmailClient unconfigured = new BrevoEmailClient(API_URL, "", SENDER_EMAIL, SENDER_NAME, RestClient.builder().build());
        assertTrue(unconfigured.sendEmail("someone@example.com", "Subject", "<p>Content</p>"));
    }
}
