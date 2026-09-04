package com.tuxlogic.shiftiq.platform.fleet.domain.model.commands;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationId;

public record DeleteEmployeeRegistrationCommand(EmployeeRegistrationId id) {
    public DeleteEmployeeRegistrationCommand {
        if (id == null) {
            throw new IllegalArgumentException("Employee Registration ID is required");
        }
    }
}
