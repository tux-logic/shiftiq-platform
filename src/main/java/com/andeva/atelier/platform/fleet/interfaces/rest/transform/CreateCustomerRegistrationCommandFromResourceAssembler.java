package com.andeva.atelier.platform.fleet.interfaces.rest.transform;

import com.andeva.atelier.platform.fleet.domain.model.commands.CreateCustomerRegistrationCommand;
import com.andeva.atelier.platform.fleet.interfaces.rest.resources.CreateCustomerRegistrationResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;

public class CreateCustomerRegistrationCommandFromResourceAssembler {

    public static CreateCustomerRegistrationCommand toCommandFromResource(CreateCustomerRegistrationResource resource) {
        return new CreateCustomerRegistrationCommand(
                new CustomerId(resource.customerId()),
                new BranchId(resource.branchId())
        );
    }
}

