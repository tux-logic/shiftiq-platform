package com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * REST payload resource for creating a Mercado Pago preference derived directly from an approved Quote.
 */
public record CreateMercadoPagoPreferenceResource(
        @NotNull(message = "billing.error.resource.quoteId.required")
        UUID quoteId
) {}
