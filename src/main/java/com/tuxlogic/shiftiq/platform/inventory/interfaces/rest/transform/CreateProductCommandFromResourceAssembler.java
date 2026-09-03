package com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.CreateProductCommand;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.InventoryQuantity;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.ProductCategory;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.ProductName;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.Sku;
import com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources.CreateProductResource;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;

public class CreateProductCommandFromResourceAssembler {
    private CreateProductCommandFromResourceAssembler() {}

    public static CreateProductCommand toCommandFromResource(CreateProductResource resource) {
        return new CreateProductCommand(
                new BranchId(resource.branchId()),
                new ProductCategory(resource.category()),
                new ProductName(resource.name()),
                new Sku(resource.sku()),
                resource.description(),
                new Money(resource.salePrice()),
                new InventoryQuantity(resource.minimumStock())
        );
    }
}
