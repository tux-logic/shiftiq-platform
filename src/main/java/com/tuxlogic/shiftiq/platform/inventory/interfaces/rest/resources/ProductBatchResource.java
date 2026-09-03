package com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductBatchResource(
        String batchId,
        Integer initialQuantity,
        Integer availableQuantity,
        BigDecimal acquisitionCost,
        Instant createdAt
) {
}
