package com.andeva.atelier.platform.iam.domain.model.valueobjects;

public record Password(String value) {
    public Password {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("iam.error.currentPassword.required");
        }
    }
}
