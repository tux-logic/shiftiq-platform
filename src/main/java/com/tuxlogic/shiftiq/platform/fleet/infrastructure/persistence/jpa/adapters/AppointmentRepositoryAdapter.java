package com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.Appointment;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.AppointmentStatus;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.AppointmentRepository;
import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.assemblers.AppointmentPersistenceAssembler;
import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.entities.AppointmentPersistenceEntity;
import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.repositories.AppointmentJpaRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public class AppointmentRepositoryAdapter implements AppointmentRepository {

    private final AppointmentJpaRepository appointmentJpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AppointmentRepositoryAdapter(AppointmentJpaRepository appointmentJpaRepository,
                                        ApplicationEventPublisher eventPublisher) {
        this.appointmentJpaRepository = appointmentJpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Appointment save(Appointment appointment) {
        AppointmentPersistenceEntity entity;
        if (appointment.getId() != null) {
            entity = appointmentJpaRepository.findById(appointment.getId())
                    .orElseGet(AppointmentPersistenceEntity::new);
        } else {
            entity = new AppointmentPersistenceEntity();
        }
        AppointmentPersistenceAssembler.toEntityFromAggregate(appointment, entity);
        var savedEntity = appointmentJpaRepository.save(entity);
        appointment.domainEvents().forEach(eventPublisher::publishEvent);
        appointment.clearDomainEvents();
        return AppointmentPersistenceAssembler.toAggregateFromEntity(savedEntity);
    }


    @Override
    public Optional<Appointment> findById(UUID appointmentId) {
        return appointmentJpaRepository.findById(appointmentId)
                .map(AppointmentPersistenceAssembler::toAggregateFromEntity);
    }

    @Override
    public boolean existsById(UUID appointmentId) {
        return appointmentJpaRepository.existsById(appointmentId);
    }

    @Override
    public void deleteById(UUID appointmentId) {
        appointmentJpaRepository.findById(appointmentId)
                .ifPresent(appointmentJpaRepository::delete);
    }

    @Override
    public boolean existsByScheduledStartLessThanAndScheduledEndGreaterThan(
            LocalDateTime scheduledEnd, LocalDateTime scheduledStart) {
        return appointmentJpaRepository
                .existsByScheduledStartLessThanAndScheduledEndGreaterThan(scheduledEnd, scheduledStart);
    }

    @Override
    public boolean existsByIdNotAndScheduledStartLessThanAndScheduledEndGreaterThan(
            UUID appointmentId, LocalDateTime scheduledEnd, LocalDateTime scheduledStart) {
        return appointmentJpaRepository
                .existsByIdNotAndScheduledStartLessThanAndScheduledEndGreaterThan(
                        appointmentId, scheduledEnd, scheduledStart);
    }

    @Override
    public List<Appointment> findByBranchId(BranchId branchId) {
        return appointmentJpaRepository.findByBranchId(branchId)
                .stream()
                .map(AppointmentPersistenceAssembler::toAggregateFromEntity)
                .toList();
    }

    @Override
    public List<Appointment> findByCustomerId(CustomerId customerId) {
        return appointmentJpaRepository.findByCustomerId(customerId)
                .stream()
                .map(AppointmentPersistenceAssembler::toAggregateFromEntity)
                .toList();
    }

    @Override
    public List<Appointment> findByVehicleId(com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId vehicleId) {
        return appointmentJpaRepository.findByVehicleId(vehicleId)
                .stream()
                .map(AppointmentPersistenceAssembler::toAggregateFromEntity)
                .toList();
    }

    @Override
    public List<Appointment> findByBranchIdAndStatus(BranchId branchId, AppointmentStatus status) {
        return appointmentJpaRepository.findByBranchIdAndStatus(branchId, status)
                .stream()
                .map(AppointmentPersistenceAssembler::toAggregateFromEntity)
                .toList();
    }
}