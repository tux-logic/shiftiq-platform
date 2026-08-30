package com.tuxlogic.shiftiq.platform.operations.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.Service;
import com.tuxlogic.shiftiq.platform.operations.interfaces.rest.resources.ServiceResource;

public class ServiceResourceFromEntityAssembler {
    public static ServiceResource toResourceFromEntity(Service entity) {
        return new ServiceResource(
                entity.getId() != null ? entity.getId().value() : null,
                entity.getBranchId() != null ? entity.getBranchId().value() : null,
                entity.getName(),
                entity.getPrice().amount().doubleValue()
        );
    }
}
