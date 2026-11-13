package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateBranchResource(
        @NotNull(message = "workshopId is required") UUID workshopId,
        @NotBlank(message = "code is required") String code,
        @NotBlank(message = "name is required") String name,
        @NotBlank(message = "address is required") String address,
        @NotBlank(message = "phone is required") String phone,
        String coverImageUrl,
        @DecimalMin(value = "-90.0", message = "latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "latitude must be <= 90")
        Double latitude,
        @DecimalMin(value = "-180.0", message = "longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "longitude must be <= 180")
        Double longitude
) {
    public CreateBranchResource(UUID workshopId, String code, String name, String address, String phone) {
        this(workshopId, code, name, address, phone, null, null, null);
    }

    public CreateBranchResource(UUID workshopId, String code, String name, String address, String phone, String coverImageUrl) {
        this(workshopId, code, name, address, phone, coverImageUrl, null, null);
    }
}
