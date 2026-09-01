package com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources;

import java.math.BigDecimal;

public record PaymentIntentResource(
        String paymentIntentId,
        String clientSecret,
        BigDecimal amount,
        String currency,
        String status
) {}
