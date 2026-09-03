package com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.UpdateProductCommand;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.InventoryQuantity;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.ProductCategory;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.ProductName;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.Sku;
import com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources.UpdateProductResource;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import java.util.UUID;

public class UpdateProductCommandFromResourceAssembler {
    private UpdateProductCommandFromResourceAssembler() {}

    public static UpdateProductCommand toCommandFromResource(UUID productId, UpdateProductResource resource) {
        return new UpdateProductCommand(
                productId,
                new ProductName(resource.name()),
                new ProductCategory(resource.category()),
                new Sku(resource.sku()),
                resource.description(),
                new Money(resource.salePrice()),
                new InventoryQuantity(resource.minimumStock())
        );
    }
}
