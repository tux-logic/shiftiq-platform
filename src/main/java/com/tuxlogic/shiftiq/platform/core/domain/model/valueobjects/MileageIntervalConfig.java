package com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects;

public record MileageIntervalConfig(int value) {
    public MileageIntervalConfig {
        if (value <= 0) {
            throw new IllegalArgumentException("core.error.mileageIntervalConfig.mustBePositive");
        }
    }
}
