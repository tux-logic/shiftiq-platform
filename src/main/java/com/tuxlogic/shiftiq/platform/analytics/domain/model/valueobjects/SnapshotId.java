package com.tuxlogic.shiftiq.platform.analytics.domain.model.valueobjects;

import java.util.UUID;

public record SnapshotId(UUID value) {
    public SnapshotId {
        if (value == null) {
            throw new IllegalArgumentException("analytics.error.snapshotId.required");
        }
    }
}
