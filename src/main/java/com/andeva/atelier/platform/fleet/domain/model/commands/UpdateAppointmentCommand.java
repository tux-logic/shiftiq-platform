package com.andeva.atelier.platform.fleet.domain.model.commands;

import com.andeva.atelier.platform.fleet.domain.model.valueobjects.AppointmentStatus;
import com.andeva.atelier.platform.fleet.domain.model.valueobjects.AppointmentSummary;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateAppointmentCommand(
        UUID appointmentId,
        BranchId branchId,
        CustomerId customerId,
        VehicleId vehicleId,
        LocalDateTime scheduledStart,
        AppointmentStatus status,
        AppointmentSummary notes
) {
    public UpdateAppointmentCommand {
        if (appointmentId == null) {
            throw new IllegalArgumentException("Appointment ID is required");
        }
        if (branchId == null) {
            throw new IllegalArgumentException("Branch ID is required");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        if (vehicleId == null) {
            throw new IllegalArgumentException("Vehicle ID is required");
        }
        if (scheduledStart == null) {
            throw new IllegalArgumentException("Scheduled start is required");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
    }
}