
package com.tuxlogic.shiftiq.platform.iot.domain.model.events;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;

public record VehicleDetailsUpdatedEvent(
        VehicleId vehicleId,
        String plateNumber,
        String brand,
        String model,
        Integer year,
        String vin
) {}