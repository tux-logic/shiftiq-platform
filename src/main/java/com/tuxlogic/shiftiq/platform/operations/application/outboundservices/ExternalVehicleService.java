package com.tuxlogic.shiftiq.platform.operations.application.outboundservices;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound ACL service interface for fetching vehicle summary details from the IoT context.
 */
public interface ExternalVehicleService {

    /**
     * Lightweight summary of a vehicle for Work Order responses.
     */
    record VehicleSummary(String plateNumber, String brand, String model, Integer year) {}

    /**
     * Fetches basic vehicle details for a given vehicle ID.
     *
     * @param vehicleId The vehicle identifier.
     * @return An Optional containing the {@link VehicleSummary} if found.
     */
    Optional<VehicleSummary> fetchVehicleSummary(UUID vehicleId);
}
