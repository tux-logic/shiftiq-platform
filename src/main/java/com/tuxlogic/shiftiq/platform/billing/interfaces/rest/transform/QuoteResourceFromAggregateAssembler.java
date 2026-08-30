package com.tuxlogic.shiftiq.platform.billing.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.QuoteResource;

public class QuoteResourceFromAggregateAssembler {
    public static QuoteResource toResourceFromAggregate(Quote aggregate) {
        return new QuoteResource(
                aggregate.getId(),
                aggregate.getWorkOrderId(),
                aggregate.getBranchId().value(),
                aggregate.getSubtotalAmount().amount(),
                aggregate.getDiscountPercentage(),
                aggregate.getTotalAmount().amount(),
                aggregate.getStatus().name()
        );
    }
}
