package com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects;

import java.util.UUID;

public record BranchReviewId(UUID value) {
    public BranchReviewId {
        if (value == null) {
            throw new IllegalArgumentException("BranchReviewId cannot be null");
        }
    }
}