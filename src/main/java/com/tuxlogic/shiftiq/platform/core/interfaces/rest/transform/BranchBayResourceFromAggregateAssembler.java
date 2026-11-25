package com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.BranchBay;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.BranchBayResource;

public class BranchBayResourceFromAggregateAssembler {

    public static BranchBayResource toResource(BranchBay bay) {
        return new BranchBayResource(
                bay.getId().value(),
                bay.getBranchId().value(),
                bay.getCode(),
                bay.getName(),
                bay.getType().name(),
                bay.isActive()
        );
    }
}