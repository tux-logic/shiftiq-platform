package com.tuxlogic.shiftiq.platform.billing.application.outboundservices;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Outbound service interface for Stripe payment gateway operations.
 * Extends generic PaymentGateway for DDD abstraction.
 */
public interface StripeGateway extends PaymentGateway {
    @Override
    default Optional<PaymentIntentResult> createPaymentIntent(BigDecimal amount, String currency, String description) {
        return createStripePaymentIntent(amount, currency, description).map(res -> new PaymentIntentResult(
                res.paymentIntentId(), res.clientSecret(), res.amount(), res.currency(), res.status()
        ));
    }

    @Override
    default Optional<PaymentIntentResult> getPaymentIntent(String paymentIntentId) {
        return getStripePaymentIntent(paymentIntentId).map(res -> new PaymentIntentResult(
                res.paymentIntentId(), res.clientSecret(), res.amount(), res.currency(), res.status()
        ));
    }

    Optional<StripePaymentIntentResult> createStripePaymentIntent(BigDecimal amount, String currency, String description);
    Optional<StripePaymentIntentResult> getStripePaymentIntent(String paymentIntentId);
}
