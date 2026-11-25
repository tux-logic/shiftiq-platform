package com.tuxlogic.shiftiq.platform.core.domain.model.queries;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BranchBayId;

public record GetBranchBayByIdQuery(BranchBayId bayId) {
    public GetBranchBayByIdQuery {
        if (bayId == null) {
            throw new IllegalArgumentException("core.error.bayId.required");
        }
    }
}