package com.tuxlogic.shiftiq.platform.billing.application.outboundservices;

import java.math.BigDecimal;

/**
 * Result object for Mercado Pago preference creation operations.
 */
public record MercadoPagoPreferenceResult(
        String preferenceId,
        String initPoint,
        String sandboxInitPoint,
        BigDecimal amount,
        String currency,
        String externalReference
) {}
