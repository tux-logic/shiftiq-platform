package com.tuxlogic.shiftiq.platform.fleet.application.queryservices;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.Appointment;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.AppointmentStatus;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppointmentQueryService {

    Result<Page<Appointment>, AppointmentQueryFailure> handle(BranchId branchId, Pageable pageable);
    Result<Page<Appointment>, AppointmentQueryFailure> handle(
            BranchId branchId, AppointmentStatus status, Pageable pageable);
    Result<Appointment, AppointmentQueryFailure> handle(UUID appointmentId);
    Result<Page<Appointment>, AppointmentQueryFailure> handle(CustomerId customerId, Pageable pageable);
    Result<Page<Appointment>, AppointmentQueryFailure> handle(VehicleId vehicleId, Pageable pageable);
}
