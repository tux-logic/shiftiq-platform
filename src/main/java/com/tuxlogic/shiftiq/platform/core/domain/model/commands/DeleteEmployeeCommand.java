package com.tuxlogic.shiftiq.platform.core.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;

public record DeleteEmployeeCommand(EmployeeId employeeId) {
    public DeleteEmployeeCommand {
        if (employeeId == null) throw new IllegalArgumentException("core.error.employeeId.required");
    }
}
