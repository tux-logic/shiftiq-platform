package com.tuxlogic.shiftiq.platform.billing.application.outboundservices;

import java.math.BigDecimal;

/**
 * Result object representing a payment retrieved from Mercado Pago.
 */
public record MercadoPagoPaymentResult(
        Long paymentId,
        String status,
        String statusDetail,
        BigDecimal amount,
        String currency,
        String externalReference
) {}
