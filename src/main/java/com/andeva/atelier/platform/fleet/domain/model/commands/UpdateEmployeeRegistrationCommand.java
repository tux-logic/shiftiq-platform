package com.andeva.atelier.platform.fleet.domain.model.commands;

import com.andeva.atelier.platform.core.domain.model.valueobjects.EmployeeId;

import java.math.BigDecimal;

public record UpdateEmployeeRegistrationCommand(
        EmployeeId id,
        String speciality,
        String specialityName,
        BigDecimal salary
) {
}
