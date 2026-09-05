package com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects;

public record GoogleId(String value) {
    public GoogleId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("iam.error.googleId.required");
        }
    }
}
