package com.tuxlogic.shiftiq.platform.billing.interfaces.rest;

import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.StripeGateway;
import com.tuxlogic.shiftiq.platform.billing.infrastructure.outbound.stripe.StripePaymentIntentResponse;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.CreatePaymentIntentResource;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.PaymentIntentResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripePaymentsControllerTest {

    @Mock
    private StripeGateway stripeGateway;

    private StripePaymentsController controller;

    @BeforeEach
    void setUp() {
        controller = new StripePaymentsController(stripeGateway);
    }

    @Test
    void createPaymentIntent_WhenGatewaySucceeds_ShouldReturnCreated() {
        // Arrange
        BigDecimal amount = new BigDecimal("100.00");
        String currency = "PEN";
        String description = "Pago taller";

        StripePaymentIntentResponse mockResponse = new StripePaymentIntentResponse(
                "pi_123456",
                "pi_123456_secret_789",
                amount,
                "PEN",
                "requires_payment_method"
        );

        when(stripeGateway.createPaymentIntent(eq(amount), eq(currency), eq(description)))
                .thenReturn(Optional.of(mockResponse));

        CreatePaymentIntentResource resource = new CreatePaymentIntentResource(amount, currency, description);

        // Act
        ResponseEntity<PaymentIntentResource> response = controller.createPaymentIntent(resource);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("pi_123456", response.getBody().paymentIntentId());
        assertEquals("pi_123456_secret_789", response.getBody().clientSecret());
        assertEquals(amount, response.getBody().amount());
        assertEquals("PEN", response.getBody().currency());
        assertEquals("requires_payment_method", response.getBody().status());
    }

    @Test
    void createPaymentIntent_WhenGatewayFails_ShouldReturnInternalServerError() {
        // Arrange
        BigDecimal amount = new BigDecimal("100.00");
        when(stripeGateway.createPaymentIntent(any(), any(), any()))
                .thenReturn(Optional.empty());

        CreatePaymentIntentResource resource = new CreatePaymentIntentResource(amount, "PEN", "Pago taller");

        // Act
        ResponseEntity<PaymentIntentResource> response = controller.createPaymentIntent(resource);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }
}
