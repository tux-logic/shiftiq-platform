package com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.CreateEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources.CreateEmployeeRegistrationResource;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

public class CreateEmployeeRegistrationCommandFromResourceAssembler {

    public static CreateEmployeeRegistrationCommand toCommandFromResource(CreateEmployeeRegistrationResource resource) {
        return new CreateEmployeeRegistrationCommand(
                new EmployeeId(resource.employeeId()),
                new BranchId(resource.branchId()),
                resource.speciality(),
                resource.specialityName(),
                resource.salary()
        );
    }
}
