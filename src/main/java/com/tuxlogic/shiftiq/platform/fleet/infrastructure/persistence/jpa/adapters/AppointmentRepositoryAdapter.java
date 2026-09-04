package com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.Appointment;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.AppointmentStatus;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.AppointmentRepository;
import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.assemblers.AppointmentPersistenceAssembler;
import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.entities.AppointmentPersistenceEntity;
import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.repositories.AppointmentJpaRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AppointmentRepositoryAdapter implements AppointmentRepository {

    private final AppointmentJpaRepository jpaRepository;

    public AppointmentRepositoryAdapter(AppointmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Appointment save(Appointment appointment) {
        var entity = AppointmentPersistenceAssembler.toEntityFromAggregate(appointment);
        var savedEntity = jpaRepository.save(entity);
        return AppointmentPersistenceAssembler.toAggregateFromEntity(savedEntity);
    }

    @Override
    public Optional<Appointment> findById(UUID appointmentId) {
        return jpaRepository.findById(appointmentId)
                .map(AppointmentPersistenceAssembler::toAggregateFromEntity);
    }

    @Override
    public boolean existsById(UUID appointmentId) {
        return jpaRepository.existsById(appointmentId);
    }

    @Override
    public void deleteById(UUID appointmentId) {
        jpaRepository.deleteById(appointmentId);
    }

    @Override
    public boolean existsOverlappingAppointment(
            BranchId branchId, VehicleId vehicleId,
            LocalDateTime scheduledEnd, LocalDateTime scheduledStart) {
        return jpaRepository
                .existsByBranchIdAndVehicleIdAndScheduledStartLessThanAndScheduledEndGreaterThan(
                        branchId, vehicleId, scheduledEnd, scheduledStart);
    }

    @Override
    public boolean existsOverlappingAppointmentExcludingId(
            UUID appointmentId, BranchId branchId, VehicleId vehicleId,
            LocalDateTime scheduledEnd, LocalDateTime scheduledStart) {
        return jpaRepository
                .existsByIdNotAndBranchIdAndVehicleIdAndScheduledStartLessThanAndScheduledEndGreaterThan(
                        appointmentId, branchId, vehicleId, scheduledEnd, scheduledStart);
    }

    @Override
    public Page<Appointment> findByBranchId(BranchId branchId, Pageable pageable) {
        return jpaRepository.findByBranchId(branchId, pageable)
                .map(AppointmentPersistenceAssembler::toAggregateFromEntity);
    }

    @Override
    public Page<Appointment> findByCustomerId(CustomerId customerId, Pageable pageable) {
        return jpaRepository.findByCustomerId(customerId, pageable)
                .map(AppointmentPersistenceAssembler::toAggregateFromEntity);
    }

    @Override
    public Page<Appointment> findByVehicleId(VehicleId vehicleId, Pageable pageable) {
        return jpaRepository.findByVehicleId(vehicleId, pageable)
                .map(AppointmentPersistenceAssembler::toAggregateFromEntity);
    }

    @Override
    public Page<Appointment> findByBranchIdAndStatus(BranchId branchId, AppointmentStatus status, Pageable pageable) {
        return jpaRepository.findByBranchIdAndStatus(branchId, status, pageable)
                .map(AppointmentPersistenceAssembler::toAggregateFromEntity);
    }
}
