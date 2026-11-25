package com.tuxlogic.shiftiq.platform.billing.application.outboundservices;

import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.PaymentProvider;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Strategy contract for pluggable virtual payment gateways (OCP / DIP).
 */
public interface PaymentGatewayStrategy {

    /**
     * @return The payment provider handled by this strategy.
     */
    PaymentProvider provider();

    /**
     * Creates a provider-hosted checkout session for the specified amount and external reference.
     */
    Optional<PaymentCheckoutSessionResult> createCheckoutSession(
            BigDecimal amount,
            String currency,
            String title,
            String externalReference
    );

    /**
     * Queries the payment status by external payment identifier.
     */
    Optional<PaymentResult> getPaymentStatusByExternalId(String externalPaymentId);
}
