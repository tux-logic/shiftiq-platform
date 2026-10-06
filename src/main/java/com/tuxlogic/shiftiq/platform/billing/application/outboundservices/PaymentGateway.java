package com.tuxlogic.shiftiq.platform.billing.application.outboundservices;

import java.util.Optional;

/**
 * Outbound service interface for payment gateway verification operations.
 */
public interface PaymentGateway {
    Optional<PaymentIntentResult> getPaymentIntent(String paymentIntentId);
}
