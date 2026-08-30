package com.andeva.atelier.platform.inventory.interfaces.rest.transform;

import com.andeva.atelier.platform.inventory.domain.model.aggregates.Product;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.ProductDetailsResource;

public class ProductDetailsResourceFromAggregateAssembler {
    public static ProductDetailsResource toResourceFromAggregate(Product aggregate) {
        var batches = aggregate.getBatches().stream()
                .map(ProductBatchResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return new ProductDetailsResource(
                aggregate.getId().toString(),
                aggregate.getBranchId().value().toString(),
                aggregate.getCategory().value(),
                aggregate.getName().name(),
                aggregate.getSku().value(),
                aggregate.getDescription(),
                aggregate.getCurrentSellingPrice().amount().doubleValue(),
                aggregate.getMinimumStock(),
                aggregate.getCurrentStock().value(),
                batches
        );
    }
}
