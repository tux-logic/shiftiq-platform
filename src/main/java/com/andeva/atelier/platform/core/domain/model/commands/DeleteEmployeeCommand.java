package com.andeva.atelier.platform.core.domain.model.commands;

import com.andeva.atelier.platform.core.domain.model.valueobjects.EmployeeId;

public record DeleteEmployeeCommand(EmployeeId employeeId) {
}
