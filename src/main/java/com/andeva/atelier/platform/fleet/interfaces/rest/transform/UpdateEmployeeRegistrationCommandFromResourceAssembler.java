package com.andeva.atelier.platform.fleet.interfaces.rest.transform;

import com.andeva.atelier.platform.core.domain.model.valueobjects.EmployeeId;
import com.andeva.atelier.platform.fleet.domain.model.commands.UpdateEmployeeRegistrationCommand;
import com.andeva.atelier.platform.fleet.interfaces.rest.resources.UpdateEmployeeRegistrationResource;

import java.util.UUID;

public class UpdateEmployeeRegistrationCommandFromResourceAssembler {
    public static UpdateEmployeeRegistrationCommand toCommandFromResource(UUID id, UpdateEmployeeRegistrationResource resource) {
        return new UpdateEmployeeRegistrationCommand(
                new EmployeeId(id),
                resource.speciality(),
                resource.specialityName(),
                resource.salary()
        );
    }
}
