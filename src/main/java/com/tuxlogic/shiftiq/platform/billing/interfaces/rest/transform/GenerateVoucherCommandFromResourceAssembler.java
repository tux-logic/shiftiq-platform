package com.tuxlogic.shiftiq.platform.billing.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.GenerateVoucherCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherType;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.GenerateVoucherResource;

public class GenerateVoucherCommandFromResourceAssembler {

    public static GenerateVoucherCommand toCommandFromResource(GenerateVoucherResource resource) {
        return new GenerateVoucherCommand(
                resource.quoteId(),
                VoucherType.valueOf(resource.type()),
                resource.customerDocumentType(),
                resource.customerDocumentNumber(),
                resource.customerName()
        );
    }
}
