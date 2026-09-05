package com.tuxlogic.shiftiq.platform.fleet.domain.model.commands;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.EmployeeId;

import java.math.BigDecimal;

public record CreateEmployeeRegistrationCommand(
        EmployeeId employeeId,
        BranchId branchId,
        String speciality,
        String specialityName,
        BigDecimal salary
) {
    public CreateEmployeeRegistrationCommand {
        if (employeeId == null) throw new IllegalArgumentException("Employee ID is required");
        if (branchId == null) throw new IllegalArgumentException("Branch ID is required");
        if (speciality == null || speciality.isBlank()) throw new IllegalArgumentException("Speciality is required");
        if (salary == null) throw new IllegalArgumentException("Salary is required");
    }
}
