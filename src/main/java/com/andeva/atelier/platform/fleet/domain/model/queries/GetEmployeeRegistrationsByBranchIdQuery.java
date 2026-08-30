package com.andeva.atelier.platform.fleet.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;

public record GetEmployeeRegistrationsByBranchIdQuery(BranchId branchId) {
}
