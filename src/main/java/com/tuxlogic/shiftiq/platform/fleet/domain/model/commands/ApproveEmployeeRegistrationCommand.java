package com.tuxlogic.shiftiq.platform.fleet.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;

public record ApproveEmployeeRegistrationCommand(
        EmployeeId registrationId
) {
    public ApproveEmployeeRegistrationCommand {
        if (registrationId == null) throw new IllegalArgumentException("Registration ID is required");
    }
}
