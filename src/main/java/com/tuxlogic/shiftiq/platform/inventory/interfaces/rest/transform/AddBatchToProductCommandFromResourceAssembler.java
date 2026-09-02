package com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.AddBatchToProductCommand;
import com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources.AddBatchToProductResource;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;

import java.math.BigDecimal;
import java.util.UUID;

public class AddBatchToProductCommandFromResourceAssembler {
    private AddBatchToProductCommandFromResourceAssembler() {}

    public static AddBatchToProductCommand toCommandFromResource(UUID productId, AddBatchToProductResource resource) {
        return new AddBatchToProductCommand(
                productId,
                resource.quantity(),
                new Money(BigDecimal.valueOf(resource.acquisitionCost()))
        );
    }
}
