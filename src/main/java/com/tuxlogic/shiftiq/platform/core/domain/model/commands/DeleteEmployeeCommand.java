package com.tuxlogic.shiftiq.platform.core.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;

public record DeleteEmployeeCommand(EmployeeId employeeId) {
}
