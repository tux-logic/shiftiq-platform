package com.andeva.atelier.platform.inventory.domain.model.commands;

import com.andeva.atelier.platform.inventory.domain.model.valueobjects.InventoryQuantity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.util.UUID;

public record AddBatchToProductCommand(
        UUID productId,
        InventoryQuantity quantity,
        Money acquisitionCost
) {
}
