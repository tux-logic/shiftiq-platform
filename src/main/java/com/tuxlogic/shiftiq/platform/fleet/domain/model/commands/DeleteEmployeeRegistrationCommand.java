package com.tuxlogic.shiftiq.platform.fleet.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;

public record DeleteEmployeeRegistrationCommand(EmployeeId id) {
    public DeleteEmployeeRegistrationCommand {
        if (id == null) {
            throw new IllegalArgumentException("EmployeeId cannot be null");
        }
    }
}
