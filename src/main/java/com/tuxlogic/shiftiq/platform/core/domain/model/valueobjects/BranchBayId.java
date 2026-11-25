package com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects;

import java.util.UUID;

public record BranchBayId(UUID value) {
    public BranchBayId {
        if (value == null) {
            throw new IllegalArgumentException("BranchBayId cannot be null");
        }
    }
}