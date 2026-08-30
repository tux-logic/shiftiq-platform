package com.andeva.atelier.platform.inventory.domain.model.commands;

import com.andeva.atelier.platform.inventory.domain.model.valueobjects.InventoryQuantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.ProductCategory;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.ProductName;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Sku;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

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
