package com.tuxlogic.shiftiq.platform.fleet.application.outboundservices;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;

/**
 * Anti-Corruption Layer (ACL) Outbound Port to verify vehicle existence in iot context.
 */
public interface ExternalVehicleService {
    boolean existsVehicleById(VehicleId vehicleId);
}
