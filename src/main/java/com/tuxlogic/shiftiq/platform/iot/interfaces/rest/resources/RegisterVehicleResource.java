package com.tuxlogic.shiftiq.platform.iot.interfaces.rest.resources;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Resource representing the request body to register a client vehicle.
 */
public record RegisterVehicleResource(
        @NotBlank(message = "Plate number is required")
        String plateNumber,

        @NotBlank(message = "Brand is required")
        String brand,

        @NotBlank(message = "Model is required")
        String model,

        @NotNull(message = "Year is required")
        @Min(value = 1900, message = "Year must be a valid manufacturing year")
        @Max(value = 2100, message = "Year must be a valid manufacturing year")
        Integer year,

        @NotBlank(message = "VIN is required")
        String vin,

        String photoUrl,

        UUID customerId
) {
    public RegisterVehicleResource(String plateNumber, String brand, String model, Integer year, String vin, String photoUrl) {
        this(plateNumber, brand, model, year, vin, photoUrl, null);
    }
}
