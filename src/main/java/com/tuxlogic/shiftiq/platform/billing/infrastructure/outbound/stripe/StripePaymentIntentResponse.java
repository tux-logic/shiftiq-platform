package com.tuxlogic.shiftiq.platform.billing.infrastructure.outbound.stripe;

import java.math.BigDecimal;

public record StripePaymentIntentResponse(
        String paymentIntentId,
        String clientSecret,
        BigDecimal amount,
        String currency,
        String status
) {}
