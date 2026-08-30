package com.andeva.atelier.platform.fleet.application.queryservices;

import com.andeva.atelier.platform.fleet.domain.model.aggregates.Appointment;
import com.andeva.atelier.platform.fleet.domain.model.valueobjects.AppointmentStatus;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.util.List;
import java.util.UUID;

public interface AppointmentQueryService {

    Result<List<Appointment>, AppointmentQueryFailure> handle(BranchId branchId);
    Result<List<Appointment>, AppointmentQueryFailure> handle(
            BranchId branchId, AppointmentStatus status);
    Result<Appointment, AppointmentQueryFailure> handle(UUID appointmentId);
    Result<List<Appointment>, AppointmentQueryFailure> handle(CustomerId customerId);
    Result<List<Appointment>, AppointmentQueryFailure> handle(VehicleId vehicleId);
}
