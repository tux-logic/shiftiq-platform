package com.tuxlogic.shiftiq.platform.inventory.domain.model.commands;

import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.StockMovementQuantity;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;

import java.util.UUID;

public record AddBatchToProductCommand(
        UUID productId,
        StockMovementQuantity quantity,
        Money acquisitionCost
) {
}
