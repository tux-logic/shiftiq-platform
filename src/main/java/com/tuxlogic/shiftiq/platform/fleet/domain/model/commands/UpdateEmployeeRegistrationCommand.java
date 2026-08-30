package com.tuxlogic.shiftiq.platform.fleet.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;

import java.math.BigDecimal;

public record UpdateEmployeeRegistrationCommand(
        EmployeeId id,
        String speciality,
        String specialityName,
        BigDecimal salary
) {
}
