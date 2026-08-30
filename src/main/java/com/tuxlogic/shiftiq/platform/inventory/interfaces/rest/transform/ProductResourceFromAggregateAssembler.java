package com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.inventory.domain.model.aggregates.Product;
import com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources.ProductResource;

public class ProductResourceFromAggregateAssembler {
    public static ProductResource toResourceFromAggregate(Product aggregate) {
        return new ProductResource(
                aggregate.getId(),
                aggregate.getBranchId().value().toString(),
                aggregate.getCategory().value(),
                aggregate.getName().name(),
                aggregate.getSku().value(),
                aggregate.getDescription(),
                aggregate.getCurrentSellingPrice().amount().doubleValue(),
                aggregate.getMinimumStock(),
                aggregate.getCurrentStock().value()
        );
    }
}
