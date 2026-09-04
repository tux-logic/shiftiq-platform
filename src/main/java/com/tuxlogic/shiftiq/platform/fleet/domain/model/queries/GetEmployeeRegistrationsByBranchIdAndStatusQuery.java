package com.tuxlogic.shiftiq.platform.fleet.domain.model.queries;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationStatus;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.springframework.data.domain.Pageable;

public record GetEmployeeRegistrationsByBranchIdAndStatusQuery(
        BranchId branchId, EmployeeRegistrationStatus status, Pageable pageable) {
}
