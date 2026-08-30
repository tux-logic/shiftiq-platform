package com.andeva.atelier.platform.fleet.domain.model.commands;

import com.andeva.atelier.platform.core.domain.model.valueobjects.EmployeeId;

public record DeleteEmployeeRegistrationCommand(EmployeeId id) {
}
