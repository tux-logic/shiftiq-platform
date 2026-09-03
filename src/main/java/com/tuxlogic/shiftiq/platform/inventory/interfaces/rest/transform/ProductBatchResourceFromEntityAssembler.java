package com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.inventory.domain.model.entities.ProductBatch;
import com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources.ProductBatchResource;

public class ProductBatchResourceFromEntityAssembler {
    public static ProductBatchResource toResourceFromEntity(ProductBatch entity) {
        return new ProductBatchResource(
                entity.getBatchId().toString(),
                entity.getInitialQuantity().value(),
                entity.getAvailableQuantity().value(),
                entity.getAcquisitionCost().amount(),
                entity.getReceptionDate()
        );
    }

    public static ProductBatchResource toResourceFromStockAdjustment(int signedQuantity, java.math.BigDecimal acquisitionCost, ProductBatch resultingBatch) {
        return new ProductBatchResource(
                resultingBatch.getBatchId().toString(),
                signedQuantity,
                resultingBatch.getAvailableQuantity().value(),
                acquisitionCost,
                resultingBatch.getReceptionDate()
        );
    }
}
