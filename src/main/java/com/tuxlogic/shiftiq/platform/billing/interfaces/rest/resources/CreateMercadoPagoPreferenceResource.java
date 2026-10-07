package com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * REST payload resource for creating a Mercado Pago preference derived directly from an approved Quote.
 * Optionally includes fiscal document data so the webhook can autonomously issue the voucher if the customer
 * completes payment without returning to the application frontend.
 */
public record CreateMercadoPagoPreferenceResource(
        @NotNull(message = "billing.error.resource.quoteId.required")
        UUID quoteId,
        String type,
        String customerDocumentType,
        String customerDocumentNumber,
        String customerName
) {
    public CreateMercadoPagoPreferenceResource(UUID quoteId) {
        this(quoteId, "RECEIPT", "DNI", "00000000", "Cliente General");
    }
}
