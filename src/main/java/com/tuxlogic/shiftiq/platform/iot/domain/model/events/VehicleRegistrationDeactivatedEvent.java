
package com.tuxlogic.shiftiq.platform.iot.domain.model.events;

import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.VehicleRegistrationId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;

import java.util.UUID;

public record VehicleRegistrationDeactivatedEvent(
        VehicleRegistrationId registrationId,
        UUID userId,
        VehicleId vehicleId
) {}