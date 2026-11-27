package com.tuxlogic.shiftiq.platform.fleet.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.AppointmentCommandFailure;
import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.AppointmentCommandService;
import com.tuxlogic.shiftiq.platform.fleet.application.outboundservices.ExternalCoreService;
import com.tuxlogic.shiftiq.platform.fleet.application.outboundservices.ExternalVehicleService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.Appointment;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.CreateAppointmentCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.DeleteAppointmentCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.UpdateAppointmentCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.AppointmentStatus;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.AppointmentRepository;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class AppointmentCommandServiceImpl implements AppointmentCommandService {

    private final AppointmentRepository appointmentRepository;
    private final ExternalCoreService externalCoreService;
    private final ExternalVehicleService externalVehicleService;

    public AppointmentCommandServiceImpl(AppointmentRepository appointmentRepository,
                                         ExternalCoreService externalCoreService,
                                         ExternalVehicleService externalVehicleService) {
        this.appointmentRepository = appointmentRepository;
        this.externalCoreService = externalCoreService;
        this.externalVehicleService = externalVehicleService;
    }

    @Override
    @Transactional
    public Result<Appointment, AppointmentCommandFailure> handle(CreateAppointmentCommand command) {
        try {
            if (!externalCoreService.existsBranchById(command.branchId())) {
                log.warn("Appointment creation failed: Branch {} does not exist", command.branchId());
                return Result.failure(AppointmentCommandFailure.INVALID_APPOINTMENT_DATA);
            }
            if (!externalCoreService.existsCustomerById(command.customerId())) {
                log.warn("Appointment creation failed: Customer {} does not exist", command.customerId());
                return Result.failure(AppointmentCommandFailure.INVALID_APPOINTMENT_DATA);
            }
            if (!externalVehicleService.existsVehicleById(command.vehicleId())) {
                log.warn("Appointment creation failed: Vehicle {} does not exist", command.vehicleId());
                return Result.failure(AppointmentCommandFailure.INVALID_APPOINTMENT_DATA);
            }
            if (command.bayId() != null
                    && !externalCoreService.existsBranchBay(command.bayId(), command.branchId().value())) {
                log.warn("Appointment creation failed: Bay {} does not belong to branch {}", command.bayId(),
                        command.branchId());
                return Result.failure(AppointmentCommandFailure.INVALID_APPOINTMENT_DATA);
            }

            var scheduledStart = command.scheduledStart();
            var scheduledEnd = scheduledStart.plusHours(1);

            if (!isWithinOperatingWindow(command.branchId().value(), scheduledStart, scheduledEnd)) {
                log.warn("Appointment creation rejected: branch {} is closed or outside operating hours at {}", command.branchId(), scheduledStart);
                return Result.failure(AppointmentCommandFailure.INVALID_APPOINTMENT_DATA);
            }

            boolean overlap = hasOverlap(null, command.branchId(), command.bayId(), scheduledEnd, scheduledStart);
            if (overlap) {
                log.warn("Appointment creation conflict: time slot overlap detected for start time {}", scheduledStart);
                return Result.failure(AppointmentCommandFailure.APPOINTMENT_ALREADY_EXISTS);
            }

            var appointment = new Appointment(
                    command.branchId(),
                    command.customerId(),
                    command.vehicleId(),
                    command.bayId(),
                    scheduledStart,
                    command.notes()
            );

            var savedAppointment = appointmentRepository.save(appointment);

            log.info("Appointment created successfully with ID {}", savedAppointment.getId());
            return Result.success(savedAppointment);

        } catch (IllegalArgumentException exception) {
            log.error("Failed to create appointment due to invalid data: {}", exception.getMessage());
            return Result.failure(AppointmentCommandFailure.INVALID_APPOINTMENT_DATA);
        }
    }

    @Override
    @Transactional
    public Result<Appointment, AppointmentCommandFailure> handle(UpdateAppointmentCommand command) {
        try {
            if (!externalCoreService.existsBranchById(command.branchId()) ||
                !externalCoreService.existsCustomerById(command.customerId()) ||
                !externalVehicleService.existsVehicleById(command.vehicleId())) {
                return Result.failure(AppointmentCommandFailure.INVALID_APPOINTMENT_DATA);
            }
            if (command.bayId() != null
                    && !externalCoreService.existsBranchBay(command.bayId(), command.branchId().value())) {
                return Result.failure(AppointmentCommandFailure.INVALID_APPOINTMENT_DATA);
            }

            var appointmentOptional = appointmentRepository.findById(command.appointmentId());

            if (appointmentOptional.isEmpty()) {
                return Result.failure(AppointmentCommandFailure.APPOINTMENT_NOT_FOUND);
            }

            var scheduledStart = command.scheduledStart();
            var scheduledEnd = scheduledStart.plusHours(1);

            if (!isWithinOperatingWindow(command.branchId().value(), scheduledStart, scheduledEnd)) {
                log.warn("Appointment update rejected: branch {} is closed or outside operating hours at {}", command.branchId(), scheduledStart);
                return Result.failure(AppointmentCommandFailure.INVALID_APPOINTMENT_DATA);
            }

            boolean overlap = hasOverlap(
                    command.appointmentId(),
                    command.branchId(),
                    command.bayId(),
                    scheduledEnd,
                    scheduledStart
            );

            if (overlap) {
                return Result.failure(AppointmentCommandFailure.APPOINTMENT_ALREADY_EXISTS);
            }

            var appointment = appointmentOptional.get();

            appointment.update(
                    command.branchId(),
                    command.customerId(),
                    command.vehicleId(),
                    command.bayId(),
                    scheduledStart,
                    command.status(),
                    command.notes()
            );

            var updatedAppointment = appointmentRepository.save(appointment);

            return Result.success(updatedAppointment);

        } catch (IllegalArgumentException exception) {
            return Result.failure(AppointmentCommandFailure.INVALID_APPOINTMENT_DATA);
        }
    }

    @Override
    @Transactional
    public Result<UUID, AppointmentCommandFailure> handle(DeleteAppointmentCommand command) {
        try {
            if (!appointmentRepository.existsById(command.appointmentId())) {
                return Result.failure(AppointmentCommandFailure.APPOINTMENT_NOT_FOUND);
            }

            appointmentRepository.deleteById(command.appointmentId());

            return Result.success(command.appointmentId());

        } catch (IllegalArgumentException exception) {
            return Result.failure(AppointmentCommandFailure.INVALID_APPOINTMENT_DATA);
        }
    }

    private boolean isWithinOperatingWindow(UUID branchId, LocalDateTime start, LocalDateTime end) {
        var windowOpt = externalCoreService.findEffectiveOperatingWindow(branchId, start.toLocalDate());
        if (windowOpt.isEmpty()) {
            return true;
        }
        var window = windowOpt.get();
        if (!window.open() || window.openTime() == null || window.closeTime() == null) {
            return false;
        }
        return start.toLocalDate().equals(end.toLocalDate())
                && !start.toLocalTime().isBefore(window.openTime())
                && !end.toLocalTime().isAfter(window.closeTime());
    }

    private boolean hasOverlap(
            UUID appointmentId,
            com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId branchId,
            UUID bayId,
            java.time.LocalDateTime scheduledEnd,
            java.time.LocalDateTime scheduledStart
    ) {
        if (bayId != null) {
            if (appointmentId == null) {
                return appointmentRepository.existsByBranchIdAndBayIdAndStatusAndScheduledStartLessThanAndScheduledEndGreaterThan(
                        branchId, bayId, AppointmentStatus.PENDING, scheduledEnd, scheduledStart);
            }
            return appointmentRepository.existsByIdNotAndBranchIdAndBayIdAndStatusAndScheduledStartLessThanAndScheduledEndGreaterThan(
                    appointmentId, branchId, bayId, AppointmentStatus.PENDING, scheduledEnd, scheduledStart);
        }
        if (appointmentId == null) {
            return appointmentRepository.existsByBranchIdAndStatusAndScheduledStartLessThanAndScheduledEndGreaterThan(
                    branchId, AppointmentStatus.PENDING, scheduledEnd, scheduledStart);
        }
        return appointmentRepository.existsByIdNotAndBranchIdAndStatusAndScheduledStartLessThanAndScheduledEndGreaterThan(
                appointmentId, branchId, AppointmentStatus.PENDING, scheduledEnd, scheduledStart);
    }
}