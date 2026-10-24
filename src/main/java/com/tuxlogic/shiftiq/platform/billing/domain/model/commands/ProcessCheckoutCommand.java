package com.tuxlogic.shiftiq.platform.billing.domain.model.commands;

import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.PaymentMethod;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.TaxDocumentValidator;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherType;

import java.util.UUID;

public record ProcessCheckoutCommand(
        UUID quoteId,
        VoucherType type,
        String customerDocumentType,
        String customerDocumentNumber,
        String customerName,
        PaymentMethod method
) {
    public ProcessCheckoutCommand {
        if (quoteId == null) {
            throw new IllegalArgumentException("billing.error.command.quoteIdRequired");
        }
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("billing.error.command.customerNameRequired");
        }
        if (method == null) {
            throw new IllegalArgumentException("billing.error.command.paymentMethodRequired");
        }
        TaxDocumentValidator.validate(type, customerDocumentType, customerDocumentNumber);
    }
}
