package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.commands.UpdateServiceCommand;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.ServiceId;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.UpdateServiceResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.math.BigDecimal;
import java.util.UUID;

public class UpdateServiceCommandFromResourceAssembler {
    public static UpdateServiceCommand toCommandFromResource(UUID ServiceId, UpdateServiceResource resource) {
        return new UpdateServiceCommand(
                new ServiceId(ServiceId),
                resource.name(),
                new Money(BigDecimal.valueOf(resource.price()))
        );
    }
}
