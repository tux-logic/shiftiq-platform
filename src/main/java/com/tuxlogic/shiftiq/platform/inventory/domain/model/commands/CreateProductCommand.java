package com.tuxlogic.shiftiq.platform.inventory.domain.model.commands;

import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.InventoryQuantity;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.ProductCategory;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.ProductName;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.Sku;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;

public record CreateProductCommand(
        BranchId branchId,
        ProductCategory category,
        ProductName name,
        Sku sku,
        String description,
        Money salePrice,
        InventoryQuantity minimumStock
) {
}
