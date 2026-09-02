package com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * REST payload resource for processing a Stripe checkout with an existing PaymentIntent ID.
 */
public record ProcessStripeCheckoutResource(
        @NotNull(message = "billing.error.resource.quoteId.required")
        UUID quoteId,
        @NotBlank(message = "billing.error.resource.type.required")
        String type,
        @NotBlank(message = "billing.error.resource.customerDocumentType.required")
        String customerDocumentType,
        @NotBlank(message = "billing.error.resource.customerDocumentNumber.required")
        String customerDocumentNumber,
        @NotBlank(message = "billing.error.resource.customerName.required")
        String customerName,
        @NotBlank(message = "billing.error.resource.paymentIntentId.required")
        String paymentIntentId
) {}
