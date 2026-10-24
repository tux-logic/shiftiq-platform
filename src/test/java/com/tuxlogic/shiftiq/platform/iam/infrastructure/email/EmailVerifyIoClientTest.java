package com.tuxlogic.shiftiq.platform.iam.infrastructure.email;

import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.email.EmailVerificationResult;
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

class EmailVerifyIoClientTest {

    private static final String API_URL = "https://app.emailverify.io/api/v1/validate";
    private static final String API_KEY = "test_key_123";

    private MockRestServiceServer mockServer;
    private EmailVerifyIoClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        RestClient restClient = restClientBuilder.build();
        client = new EmailVerifyIoClient(API_URL, API_KEY, restClient);
    }

    @Test
    void verifyEmail_WhenStatusIsValid_ShouldReturnValidResult() {
        String jsonResponse = """
                {
                    "email": "valid@gmail.com",
                    "status": "valid",
                    "sub_status": "permitted"
                }
                """;

        mockServer.expect(requestTo(API_URL + "?key=" + API_KEY + "&email=valid@gmail.com"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        EmailVerificationResult result = client.verifyEmail("valid@gmail.com");

        assertTrue(result.valid());
        assertEquals("valid", result.status());
        assertEquals("permitted", result.subStatus());
        mockServer.verify();
    }

    @Test
    void verifyEmail_WhenStatusIsInvalid_ShouldReturnInvalidResult() {
        String jsonResponse = """
                {
                    "email": "invalid@bad-domain.com",
                    "status": "invalid",
                    "sub_status": "mailbox_not_found"
                }
                """;

        mockServer.expect(requestTo(API_URL + "?key=" + API_KEY + "&email=invalid@bad-domain.com"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        EmailVerificationResult result = client.verifyEmail("invalid@bad-domain.com");

        assertFalse(result.valid());
        assertEquals("invalid", result.status());
        assertEquals("mailbox_not_found", result.subStatus());
        mockServer.verify();
    }

    @Test
    void verifyEmail_WhenStatusIsDoNotMail_ShouldReturnInvalidResult() {
        String jsonResponse = """
                {
                    "email": "spamtrap@trashmail.com",
                    "status": "do-not-mail",
                    "sub_status": "disposable"
                }
                """;

        mockServer.expect(requestTo(API_URL + "?key=" + API_KEY + "&email=spamtrap@trashmail.com"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        EmailVerificationResult result = client.verifyEmail("spamtrap@trashmail.com");

        assertFalse(result.valid());
        assertEquals("do-not-mail", result.status());
        assertEquals("disposable", result.subStatus());
        mockServer.verify();
    }

    @Test
    void verifyEmail_WhenApiReturnsErrorPayload_ShouldReturnInvalidResult() {
        String jsonResponse = """
                {
                    "error": "0 credits remaining"
                }
                """;

        mockServer.expect(requestTo(API_URL + "?key=" + API_KEY + "&email=test@example.com"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        EmailVerificationResult result = client.verifyEmail("test@example.com");

        assertFalse(result.valid());
        assertEquals("error", result.status());
        assertEquals("api_error", result.subStatus());
        mockServer.verify();
    }

    @Test
    void verifyEmail_WhenServerFailsWith500_ShouldHandleExceptionGracefully() {
        mockServer.expect(requestTo(API_URL + "?key=" + API_KEY + "&email=timeout@example.com"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        EmailVerificationResult result = client.verifyEmail("timeout@example.com");

        assertFalse(result.valid());
        assertEquals("error", result.status());
        assertEquals("communication_failure", result.subStatus());
        mockServer.verify();
    }

    @Test
    void verifyEmail_WhenEmailIsNullOrEmpty_ShouldReturnInvalidImmediately() {
        EmailVerificationResult resultNull = client.verifyEmail(null);
        assertFalse(resultNull.valid());
        assertEquals("empty", resultNull.subStatus());

        EmailVerificationResult resultBlank = client.verifyEmail("   ");
        assertFalse(resultBlank.valid());
        assertEquals("empty", resultBlank.subStatus());
    }

    @Test
    void verifyEmail_WhenApiKeyIsUnconfigured_ShouldFallbackToValidBypass() {
        EmailVerifyIoClient unconfiguredClient = new EmailVerifyIoClient(API_URL, "", RestClient.builder().build());
        EmailVerificationResult result = unconfiguredClient.verifyEmail("someone@example.com");

        assertTrue(result.valid());
        assertEquals("unconfigured", result.status());
        assertEquals("bypassed", result.subStatus());
    }
}
