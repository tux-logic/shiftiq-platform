package com.tuxlogic.shiftiq.platform.core.domain.model.queries;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

public record GetBranchBaysByBranchIdQuery(BranchId branchId, boolean activeOnly) {
    public GetBranchBaysByBranchIdQuery {
        if (branchId == null) {
            throw new IllegalArgumentException("core.error.branchId.required");
        }
    }
}