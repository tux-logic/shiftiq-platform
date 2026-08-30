package com.andeva.atelier.platform.fleet.domain.model.queries;

import com.andeva.atelier.platform.fleet.domain.model.valueobjects.EmployeeRegistrationStatus;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;

public record GetEmployeeRegistrationsByBranchIdAndStatusQuery(BranchId branchId, EmployeeRegistrationStatus status) {
}
