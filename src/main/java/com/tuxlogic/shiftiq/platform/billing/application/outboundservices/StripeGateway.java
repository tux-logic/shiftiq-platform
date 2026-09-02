package com.tuxlogic.shiftiq.platform.billing.application.outboundservices;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Outbound service interface for Stripe payment gateway operations.
 */
public interface StripeGateway {
    Optional<StripePaymentIntentResult> createPaymentIntent(BigDecimal amount, String currency, String description);
    Optional<StripePaymentIntentResult> getPaymentIntent(String paymentIntentId);
}
