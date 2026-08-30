package com.tuxlogic.shiftiq.platform.operations.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.operations.domain.model.commands.CreateServiceCommand;
import com.tuxlogic.shiftiq.platform.operations.interfaces.rest.resources.CreateServiceResource;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;

import java.math.BigDecimal;

public class CreateServiceCommandFromResourceAssembler {
    public static CreateServiceCommand toCommandFromResource(CreateServiceResource resource) {
        return new CreateServiceCommand(
                new BranchId(resource.branchId()),
                resource.name(),
                new Money(BigDecimal.valueOf(resource.price()))
        );
    }
}
