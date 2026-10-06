package com.tuxlogic.shiftiq.platform.fleet.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.math.BigDecimal;

public record RequestEmployeeJoinCommand(
        EmployeeId employeeId,
        BranchId branchId,
        String speciality,
        String specialityName,
        BigDecimal salary
) {
    public RequestEmployeeJoinCommand {
        if (employeeId == null) throw new IllegalArgumentException("Employee ID is required");
        if (branchId == null) throw new IllegalArgumentException("Branch ID is required");
    }
}
