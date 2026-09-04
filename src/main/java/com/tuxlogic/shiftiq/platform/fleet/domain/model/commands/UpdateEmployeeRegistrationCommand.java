package com.tuxlogic.shiftiq.platform.fleet.domain.model.commands;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationId;

import java.math.BigDecimal;

public record UpdateEmployeeRegistrationCommand(
        EmployeeRegistrationId id,
        String speciality,
        String specialityName,
        BigDecimal salary
) {
    public UpdateEmployeeRegistrationCommand {
        if (id == null) {
            throw new IllegalArgumentException("Employee Registration ID is required");
        }
        if (speciality == null || speciality.isBlank()) {
            throw new IllegalArgumentException("Speciality is required");
        }
        if (salary == null || salary.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Salary must be greater than or equal to zero");
        }
    }
}
