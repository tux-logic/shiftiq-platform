package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public record UpdateBranchResource(
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
    public UpdateBranchResource(String code, String name, String address, String phone) {
        this(code, name, address, phone, null, null, null);
    }

    public UpdateBranchResource(String code, String name, String address, String phone, String coverImageUrl) {
        this(code, name, address, phone, coverImageUrl, null, null);
    }
}
