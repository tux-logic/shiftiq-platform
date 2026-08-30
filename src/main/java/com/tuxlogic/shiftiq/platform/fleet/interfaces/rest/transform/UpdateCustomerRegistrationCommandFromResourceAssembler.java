package com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.UpdateCustomerRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.CustomerRegistrationStatus;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources.UpdateCustomerRegistrationResource;

import java.util.UUID;

public class UpdateCustomerRegistrationCommandFromResourceAssembler {

    public static UpdateCustomerRegistrationCommand toCommandFromResource(UUID registrationId, UpdateCustomerRegistrationResource resource) {
        return new UpdateCustomerRegistrationCommand(
                registrationId,
                new CustomerRegistrationStatus(resource.status())
        );
    }
}

