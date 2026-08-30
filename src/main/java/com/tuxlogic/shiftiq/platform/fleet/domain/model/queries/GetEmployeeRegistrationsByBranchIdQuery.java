package com.tuxlogic.shiftiq.platform.fleet.domain.model.queries;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

public record GetEmployeeRegistrationsByBranchIdQuery(BranchId branchId) {
}
