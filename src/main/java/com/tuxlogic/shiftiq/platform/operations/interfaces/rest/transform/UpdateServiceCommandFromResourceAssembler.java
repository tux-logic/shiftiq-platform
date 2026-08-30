package com.tuxlogic.shiftiq.platform.operations.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.operations.domain.model.commands.UpdateServiceCommand;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.ServiceId;
import com.tuxlogic.shiftiq.platform.operations.interfaces.rest.resources.UpdateServiceResource;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;

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
