package com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.CreateCustomerRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources.CreateCustomerRegistrationResource;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;

public class CreateCustomerRegistrationCommandFromResourceAssembler {

    public static CreateCustomerRegistrationCommand toCommandFromResource(CreateCustomerRegistrationResource resource) {
        return new CreateCustomerRegistrationCommand(
                new CustomerId(resource.customerId()),
                new BranchId(resource.branchId())
        );
    }
}

