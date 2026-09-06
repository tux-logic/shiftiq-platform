package com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.AddBatchToProductCommand;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.StockMovementQuantity;
import com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources.AddBatchToProductResource;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import java.util.UUID;

public class AddBatchToProductCommandFromResourceAssembler {
    private AddBatchToProductCommandFromResourceAssembler() {}

    public static AddBatchToProductCommand toCommandFromResource(UUID productId, AddBatchToProductResource resource) {
        return new AddBatchToProductCommand(
                productId,
                new StockMovementQuantity(resource.quantity()),
                Money.of(resource.acquisitionCost() != null ? resource.acquisitionCost() : 0.0)
        );
    }
}
