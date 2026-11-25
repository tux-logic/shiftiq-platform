package com.tuxlogic.shiftiq.platform.billing.application.outboundservices;

import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.PaymentProvider;

import java.math.BigDecimal;

/**
 * Provider-agnostic checkout session returned by a {@link PaymentGatewayStrategy}.
 */
public record PaymentCheckoutSessionResult(
        PaymentProvider provider,
        String sessionId,
        String checkoutUrl,
        String sandboxCheckoutUrl,
        BigDecimal amount,
        String currency,
        String externalReference
) {
}
