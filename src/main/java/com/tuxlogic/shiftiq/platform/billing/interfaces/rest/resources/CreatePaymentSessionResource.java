package com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources;

import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.PaymentProvider;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request payload for creating a provider-agnostic virtual payment checkout session.
 */
public record CreatePaymentSessionResource(
        @NotNull(message = "quoteId is required") UUID quoteId,
        PaymentProvider provider,
        String type,
        String customerDocumentType,
        String customerDocumentNumber,
        String customerName
) {
}
