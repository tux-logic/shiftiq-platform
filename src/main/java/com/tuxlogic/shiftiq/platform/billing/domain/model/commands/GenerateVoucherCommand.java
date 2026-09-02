package com.tuxlogic.shiftiq.platform.billing.domain.model.commands;

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
    }
}
