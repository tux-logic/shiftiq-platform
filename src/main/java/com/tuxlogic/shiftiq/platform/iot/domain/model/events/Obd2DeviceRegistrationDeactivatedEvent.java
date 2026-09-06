// Obd2DeviceRegistrationDeactivatedEvent.java
package com.tuxlogic.shiftiq.platform.iot.domain.model.events;

import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.Obd2DeviceId;
import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.Obd2DeviceRegistrationId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;

public record Obd2DeviceRegistrationDeactivatedEvent(
        Obd2DeviceRegistrationId registrationId,
        Obd2DeviceId obd2DeviceId,
        BranchId branchId,
        VehicleId vehicleId
) {}