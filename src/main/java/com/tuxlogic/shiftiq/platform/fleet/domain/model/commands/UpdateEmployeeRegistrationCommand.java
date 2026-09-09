package com.tuxlogic.shiftiq.platform.fleet.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;

import java.math.BigDecimal;

public record UpdateEmployeeRegistrationCommand(
        EmployeeId id,
        String speciality,
        String specialityName,
        BigDecimal salary
) {
    public UpdateEmployeeRegistrationCommand {
        if (id == null) {
            throw new IllegalArgumentException("EmployeeId cannot be null");
        }
        if (speciality == null || speciality.isBlank()) {
            throw new IllegalArgumentException("Speciality cannot be null or blank");
        }
        if (salary == null || salary.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Salary cannot be null or negative");
        }
    }
}
