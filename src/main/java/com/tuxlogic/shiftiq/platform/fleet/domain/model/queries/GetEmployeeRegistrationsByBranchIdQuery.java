package com.tuxlogic.shiftiq.platform.fleet.domain.model.queries;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.springframework.data.domain.Pageable;

public record GetEmployeeRegistrationsByBranchIdQuery(BranchId branchId, Pageable pageable) {
}
