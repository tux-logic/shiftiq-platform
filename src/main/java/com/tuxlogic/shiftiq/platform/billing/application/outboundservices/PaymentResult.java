package com.tuxlogic.shiftiq.platform.billing.application.outboundservices;

import java.math.BigDecimal;

/**
 * Result DTO representing payment status and gateway details in application layer.
 */
public record PaymentResult(
        String paymentId,
        String externalReference,
        BigDecimal amount,
        String currency,
        String status
) {}
