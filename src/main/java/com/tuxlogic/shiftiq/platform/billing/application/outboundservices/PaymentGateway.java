package com.tuxlogic.shiftiq.platform.billing.application.outboundservices;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Outbound service interface for payment gateway operations.
 */
public interface PaymentGateway {
    Optional<PaymentIntentResult> createPaymentIntent(BigDecimal amount, String currency, String description);
    Optional<PaymentIntentResult> getPaymentIntent(String paymentIntentId);
}
