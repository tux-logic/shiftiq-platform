package com.tuxlogic.shiftiq.platform.fleet.domain.model.events;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when a new Appointment is scheduled in fleet context.
 */
public record AppointmentCreatedEvent(
        Object source,
        UUID appointmentId,
        BranchId branchId,
        CustomerId customerId,
        VehicleId vehicleId,
        LocalDateTime scheduledStart
) {}
