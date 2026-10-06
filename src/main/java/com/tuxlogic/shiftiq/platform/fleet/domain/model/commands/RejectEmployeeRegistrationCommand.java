package com.tuxlogic.shiftiq.platform.fleet.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;

public record RejectEmployeeRegistrationCommand(
        EmployeeId registrationId,
        String reason
) {
    public RejectEmployeeRegistrationCommand {
        if (registrationId == null) throw new IllegalArgumentException("Registration ID is required");
    }
}
