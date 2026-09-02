package com.tuxlogic.shiftiq.platform.billing.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.GenerateVoucherCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherType;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.GenerateVoucherResource;

public class GenerateVoucherCommandFromResourceAssembler {

    public static GenerateVoucherCommand toCommandFromResource(GenerateVoucherResource resource) {
        VoucherType type;
        try {
            type = resource.type() != null ? VoucherType.valueOf(resource.type().toUpperCase()) : VoucherType.RECEIPT;
        } catch (Exception e) {
            throw new IllegalArgumentException("billing.error.voucher.invalidType");
        }
        return new GenerateVoucherCommand(
                resource.quoteId(),
                type,
                resource.customerDocumentType(),
                resource.customerDocumentNumber(),
                resource.customerName()
        );
    }
}
