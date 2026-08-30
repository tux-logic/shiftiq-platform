package com.andeva.atelier.platform.inventory.interfaces.rest.transform;

import com.andeva.atelier.platform.inventory.domain.model.entities.ProductBatch;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.ProductBatchResource;

public class ProductBatchResourceFromEntityAssembler {
    public static ProductBatchResource toResourceFromEntity(ProductBatch entity) {
        return new ProductBatchResource(
                entity.getBatchId().toString(),
                entity.getInitialQuantity().value(),
                entity.getAvailableQuantity().value(),
                entity.getAcquisitionCost().amount().doubleValue(),
                entity.getReceptionDate()
        );
    }
}
