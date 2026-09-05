package com.tuxlogic.shiftiq.platform.fleet.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.AppointmentQueryFailure;
import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.AppointmentQueryService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.Appointment;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.AppointmentStatus;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.AppointmentRepository;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AppointmentQueryServiceImpl implements AppointmentQueryService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentQueryServiceImpl(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public Result<Page<Appointment>, AppointmentQueryFailure> handle(BranchId branchId, Pageable pageable) {
        try {
            var appointments = appointmentRepository.findByBranchId(branchId, pageable);
            return Result.success(appointments);
        } catch (IllegalArgumentException e) {
            return Result.failure(AppointmentQueryFailure.INVALID_QUERY_PARAMS);
        }
    }

    @Override
    public Result<Page<Appointment>, AppointmentQueryFailure> handle(
            BranchId branchId, AppointmentStatus status, Pageable pageable) {
        try {
            var appointments = appointmentRepository.findByBranchIdAndStatus(branchId, status, pageable);
            return Result.success(appointments);
        } catch (IllegalArgumentException e) {
            return Result.failure(AppointmentQueryFailure.INVALID_QUERY_PARAMS);
        }
    }

    @Override
    public Result<Appointment, AppointmentQueryFailure> handle(UUID appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .map(Result::<Appointment, AppointmentQueryFailure>success)
                .orElse(Result.failure(AppointmentQueryFailure.APPOINTMENT_NOT_FOUND));
    }

    @Override
    public Result<Page<Appointment>, AppointmentQueryFailure> handle(com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId customerId, Pageable pageable) {
        try {
            var appointments = appointmentRepository.findByCustomerId(customerId, pageable);
            return Result.success(appointments);
        } catch (IllegalArgumentException e) {
            return Result.failure(AppointmentQueryFailure.INVALID_QUERY_PARAMS);
        }
    }

    @Override
    public Result<Page<Appointment>, AppointmentQueryFailure> handle(com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId vehicleId, Pageable pageable) {
        try {
            var appointments = appointmentRepository.findByVehicleId(vehicleId, pageable);
            return Result.success(appointments);
        } catch (IllegalArgumentException e) {
            return Result.failure(AppointmentQueryFailure.INVALID_QUERY_PARAMS);
        }
    }
}
