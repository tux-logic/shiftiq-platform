package com.tuxlogic.shiftiq.platform.billing.domain.model.commands;

import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.TaxDocumentValidator;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherType;

import java.util.UUID;

/**
 * Command representing a Mercado Pago checkout process where a Mercado Pago paymentId is verified before voucher generation.
 */
public record ProcessMercadoPagoCheckoutCommand(
        UUID quoteId,
        VoucherType type,
        String customerDocumentType,
        String customerDocumentNumber,
        String customerName,
        String paymentId
) {
    public ProcessMercadoPagoCheckoutCommand {
        if (quoteId == null) {
            throw new IllegalArgumentException("billing.error.command.quoteIdRequired");
        }
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("billing.error.command.customerNameRequired");
        }
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("billing.error.command.paymentIdRequired");
        }
        TaxDocumentValidator.validate(type, customerDocumentType, customerDocumentNumber);
    }
}
