package com.andeva.atelier.platform.core.domain.model.queries;

import com.andeva.atelier.platform.core.domain.model.valueobjects.UserId;

public record GetProfileRolesByUserIdQuery(UserId userId) {
    public GetProfileRolesByUserIdQuery {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
    }
}
