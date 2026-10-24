package com.tuxlogic.shiftiq.platform.billing.domain.model.commands;

import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.TaxDocumentValidator;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherType;

import java.util.UUID;

public record GenerateVoucherCommand(
        UUID quoteId,
        VoucherType type,
        String customerDocumentType,
        String customerDocumentNumber,
        String customerName
) {
    public GenerateVoucherCommand {
        if (quoteId == null) {
            throw new IllegalArgumentException("billing.error.command.quoteIdRequired");
        }
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("billing.error.command.customerNameRequired");
        }
        TaxDocumentValidator.validate(type, customerDocumentType, customerDocumentNumber);
    }
}
