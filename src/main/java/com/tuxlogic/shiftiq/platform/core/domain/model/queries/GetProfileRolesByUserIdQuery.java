package com.tuxlogic.shiftiq.platform.core.domain.model.queries;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;

public record GetProfileRolesByUserIdQuery(UserId userId) {
    public GetProfileRolesByUserIdQuery {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
    }
}
