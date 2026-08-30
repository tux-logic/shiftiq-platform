package com.andeva.atelier.platform.fleet.domain.model.commands;

import com.andeva.atelier.platform.fleet.domain.model.valueobjects.AppointmentSummary;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.time.LocalDateTime;

public record CreateAppointmentCommand(
        BranchId branchId,
        CustomerId customerId,
        VehicleId vehicleId,
        LocalDateTime scheduledStart,
        AppointmentSummary notes
) {
    public CreateAppointmentCommand {
        if (branchId == null) throw new IllegalArgumentException("Branch ID is required");
        if (customerId == null) throw new IllegalArgumentException("Customer ID is required");
        if (vehicleId == null) throw new IllegalArgumentException("Vehicle ID is required");
        if (scheduledStart == null) throw new IllegalArgumentException("Scheduled start is required");
    }
}