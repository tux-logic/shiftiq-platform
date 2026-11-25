package com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources;

import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.PaymentProvider;

import java.math.BigDecimal;

/**
 * Provider-agnostic checkout session response returned to web and mobile clients.
 */
public record PaymentSessionResource(
        PaymentProvider provider,
        String sessionId,
        String checkoutUrl,
        String sandboxCheckoutUrl,
        BigDecimal amount,
        String currency,
        String externalReference
) {
}
