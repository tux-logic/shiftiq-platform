package com.andeva.atelier.platform.billing.domain.model.commands;

import com.andeva.atelier.platform.billing.domain.model.valueobjects.VoucherType;

import java.util.UUID;

public record GenerateVoucherCommand(
        UUID quoteId,
        VoucherType type,
        String customerDocumentType,
        String customerDocumentNumber,
        String customerName
) {
}
