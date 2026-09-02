package com.tuxlogic.shiftiq.platform.billing.application.commandservices;

import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.StripePaymentIntentResult;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Application command service interface for Stripe payment domain operations.
 */
public interface StripePaymentCommandService {
    Optional<StripePaymentIntentResult> createPaymentIntent(BigDecimal amount, String currency, String description);
    Optional<StripePaymentIntentResult> getPaymentIntent(String paymentIntentId);
}
