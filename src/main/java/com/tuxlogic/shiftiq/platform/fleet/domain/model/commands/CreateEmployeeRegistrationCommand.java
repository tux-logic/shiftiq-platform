package com.tuxlogic.shiftiq.platform.fleet.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.math.BigDecimal;
import java.util.Set;

public record CreateEmployeeRegistrationCommand(
        EmployeeId employeeId,
        BranchId branchId,
        String speciality,
        String specialityName,
        BigDecimal salary,
        String role
) {
    private static final Set<String> ALLOWED_ROLES =
            Set.of("ROLE_BRANCH_MANAGER", "ROLE_ASSISTANT", "ROLE_EMPLOYEE");

    public CreateEmployeeRegistrationCommand {
        if (employeeId == null) throw new IllegalArgumentException("Employee ID is required");
        if (branchId == null) throw new IllegalArgumentException("Branch ID is required");
        if (speciality == null || speciality.isBlank()) throw new IllegalArgumentException("Speciality is required");
        if (salary == null) throw new IllegalArgumentException("Salary is required");
        if (role == null || role.isBlank()) {
            role = "ROLE_EMPLOYEE";
        } else {
            role = role.trim().toUpperCase();
            if (!role.startsWith("ROLE_")) {
                role = "ROLE_" + role;
            }
        }
        if (!ALLOWED_ROLES.contains(role)) {
            throw new IllegalArgumentException("fleet.error.resource.role.invalid");
        }
    }

    public CreateEmployeeRegistrationCommand(EmployeeId employeeId, BranchId branchId,
                                              String speciality, String specialityName, BigDecimal salary) {
        this(employeeId, branchId, speciality, specialityName, salary, "ROLE_EMPLOYEE");
    }
}
