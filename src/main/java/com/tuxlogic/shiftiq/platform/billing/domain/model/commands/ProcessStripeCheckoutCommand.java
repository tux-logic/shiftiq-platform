package com.tuxlogic.shiftiq.platform.billing.domain.model.commands;

import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherType;

import java.util.UUID;

/**
 * Command representing a Stripe checkout process where a Stripe paymentIntentId is verified before voucher generation.
 */
public record ProcessStripeCheckoutCommand(
        UUID quoteId,
        VoucherType type,
        String customerDocumentType,
        String customerDocumentNumber,
        String customerName,
        String paymentIntentId
) {
    public ProcessStripeCheckoutCommand {
        if (quoteId == null) {
            throw new IllegalArgumentException("billing.error.command.quoteIdRequired");
        }
        if (type == null) {
            throw new IllegalArgumentException("billing.error.command.voucherTypeRequired");
        }
        if (customerDocumentType == null || customerDocumentType.isBlank()) {
            throw new IllegalArgumentException("billing.error.command.customerDocumentTypeRequired");
        }
        if (customerDocumentNumber == null || customerDocumentNumber.isBlank()) {
            throw new IllegalArgumentException("billing.error.command.customerDocumentNumberRequired");
        }
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("billing.error.command.customerNameRequired");
        }
        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            throw new IllegalArgumentException("billing.error.command.paymentIntentIdRequired");
        }
    }
}
