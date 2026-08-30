package com.tuxlogic.shiftiq.platform.inventory.domain.model.commands;

import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.ProductCategory;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.ProductName;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.Sku;

import java.util.UUID;

public record UpdateProductCommand(
        UUID productId,
        ProductName name,
        ProductCategory category,
        Sku sku,
        String description,
        com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money salePrice,
        com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.InventoryQuantity minimumStock
) {
}
