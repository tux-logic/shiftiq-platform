package com.tuxlogic.shiftiq.platform.fleet.domain.model.queries;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.util.UUID;

public record GetStaffCareerHistoryByEmployeeAndBranchQuery(BranchId branchId, UUID employeeId) {
    public GetStaffCareerHistoryByEmployeeAndBranchQuery {
        if (branchId == null) {
            throw new IllegalArgumentException("fleet.error.branchId.required");
        }
        if (employeeId == null) {
            throw new IllegalArgumentException("fleet.error.employeeId.required");
        }
    }
}
