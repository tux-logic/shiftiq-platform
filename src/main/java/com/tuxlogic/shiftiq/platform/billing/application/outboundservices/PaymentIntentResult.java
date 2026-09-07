package com.tuxlogic.shiftiq.platform.billing.application.outboundservices;

import java.math.BigDecimal;

/**
 * Result DTO representing created PaymentIntent in application layer.
 */
public record PaymentIntentResult(
        String paymentIntentId,
        String clientSecret,
        BigDecimal amount,
        String currency,
        String status
) {}
