package com.tuxlogic.shiftiq.platform.fleet.application.queryservices;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.Appointment;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.AppointmentStatus;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;

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
