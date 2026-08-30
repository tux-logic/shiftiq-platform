package com.andeva.atelier.platform.billing.interfaces.rest.transform;

import com.andeva.atelier.platform.billing.domain.model.commands.GenerateVoucherCommand;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.VoucherType;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.GenerateVoucherResource;

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
