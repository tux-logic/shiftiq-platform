package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateWorkshopResource(
        @NotNull(message = "ownerId is required") UUID ownerId,
        @NotBlank(message = "businessName is required") String businessName,
        @NotBlank(message = "brandName is required") String brandName,
        @NotBlank(message = "taxId is required") String taxId,
        @Min(value = 1, message = "mileageIntervalConfig must be greater than 0") int mileageIntervalConfig
) {
}
