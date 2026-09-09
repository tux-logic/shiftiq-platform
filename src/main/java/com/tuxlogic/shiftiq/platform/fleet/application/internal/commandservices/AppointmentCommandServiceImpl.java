package com.tuxlogic.shiftiq.platform.fleet.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.AppointmentCommandFailure;
import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.AppointmentCommandService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.Appointment;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.CreateAppointmentCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.DeleteAppointmentCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.UpdateAppointmentCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.AppointmentRepository;
import com.tuxlogic.shiftiq.platform.fleet.application.outboundservices.ExternalCoreService;
import com.tuxlogic.shiftiq.platform.fleet.application.outboundservices.ExternalVehicleService;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            var scheduledStart = command.scheduledStart();
            var scheduledEnd = scheduledStart.plusHours(1);

            boolean overlap = appointmentRepository.existsByScheduledStartLessThanAndScheduledEndGreaterThan(
                    scheduledEnd,
                    scheduledStart
            );

            if (overlap) {
                log.warn("Appointment creation conflict: time slot overlap detected for start time {}", scheduledStart);
                return Result.failure(AppointmentCommandFailure.APPOINTMENT_ALREADY_EXISTS);
            }

            var appointment = new Appointment(
                    command.branchId(),
                    command.customerId(),
                    command.vehicleId(),
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
            var appointmentOptional = appointmentRepository.findById(command.appointmentId());

            if (appointmentOptional.isEmpty()) {
                return Result.failure(AppointmentCommandFailure.APPOINTMENT_NOT_FOUND);
            }

            var scheduledStart = command.scheduledStart();
            var scheduledEnd = scheduledStart.plusHours(1);

            boolean overlap = appointmentRepository.existsByIdNotAndScheduledStartLessThanAndScheduledEndGreaterThan(
                    command.appointmentId(),
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
}