package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateBranchReviewResource(
        @NotNull UUID customerId,
        UUID appointmentId,
        @Min(1) @Max(5) int rating,
        @Size(max = 1000) String comment
) {
}