package com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.UpdateEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationId;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources.UpdateEmployeeRegistrationResource;

import java.util.UUID;

public class UpdateEmployeeRegistrationCommandFromResourceAssembler {
    public static UpdateEmployeeRegistrationCommand toCommandFromResource(UUID employeeRegistrationId, UpdateEmployeeRegistrationResource resource) {
        return new UpdateEmployeeRegistrationCommand(
                new EmployeeRegistrationId(employeeRegistrationId),
                resource.speciality(),
                resource.specialityName(),
                resource.salary()
        );
    }
}
