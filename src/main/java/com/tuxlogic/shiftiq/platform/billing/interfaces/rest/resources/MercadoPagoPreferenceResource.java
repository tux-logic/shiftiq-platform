package com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources;

import java.math.BigDecimal;

public record MercadoPagoPreferenceResource(
        String preferenceId,
        String initPoint,
        String sandboxInitPoint,
        BigDecimal amount,
        String currency,
        String externalReference
) {}
