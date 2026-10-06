package com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.RequestEmployeeJoinCommand;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources.RequestEmployeeJoinResource;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

public class RequestEmployeeJoinCommandFromResourceAssembler {
    public static RequestEmployeeJoinCommand toCommandFromResource(RequestEmployeeJoinResource resource) {
        return new RequestEmployeeJoinCommand(
                new EmployeeId(resource.employeeId()),
                new BranchId(resource.branchId()),
                resource.speciality(),
                resource.specialityName(),
                resource.salary()
        );
    }
}
