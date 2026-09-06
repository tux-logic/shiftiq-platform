package com.tuxlogic.shiftiq.platform.iot.domain.services;

import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.ActiveRegistrationContext;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;

import java.util.Optional;

/**
 * Domain service interface to resolve active registration context for a vehicle.
 */
public interface ActiveRegistrationContextService {
    Optional<ActiveRegistrationContext> resolveActiveContextForVehicle(VehicleId vehicleId);
}
