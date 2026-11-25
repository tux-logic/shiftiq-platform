package com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.AppointmentStatus;
import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.entities.AppointmentPersistenceEntity;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentJpaRepository extends JpaRepository<AppointmentPersistenceEntity, UUID> {

        boolean existsByScheduledStartLessThanAndScheduledEndGreaterThan(
                        LocalDateTime scheduledEnd, LocalDateTime scheduledStart);

        boolean existsByIdNotAndScheduledStartLessThanAndScheduledEndGreaterThan(
                        UUID appointmentId, LocalDateTime scheduledEnd, LocalDateTime scheduledStart);

        boolean existsByBranchIdAndBayIdAndStatusAndScheduledStartLessThanAndScheduledEndGreaterThan(
                        BranchId branchId,
                        UUID bayId,
                        AppointmentStatus status,
                        LocalDateTime scheduledEnd,
                        LocalDateTime scheduledStart);

        boolean existsByIdNotAndBranchIdAndBayIdAndStatusAndScheduledStartLessThanAndScheduledEndGreaterThan(
                        UUID appointmentId,
                        BranchId branchId,
                        UUID bayId,
                        AppointmentStatus status,
                        LocalDateTime scheduledEnd,
                        LocalDateTime scheduledStart);

        boolean existsByBranchIdAndStatusAndScheduledStartLessThanAndScheduledEndGreaterThan(
                        BranchId branchId,
                        AppointmentStatus status,
                        LocalDateTime scheduledEnd,
                        LocalDateTime scheduledStart);

        boolean existsByIdNotAndBranchIdAndStatusAndScheduledStartLessThanAndScheduledEndGreaterThan(
                        UUID appointmentId,
                        BranchId branchId,
                        AppointmentStatus status,
                        LocalDateTime scheduledEnd,
                        LocalDateTime scheduledStart);

        List<AppointmentPersistenceEntity> findByBranchIdAndScheduledStartBetween(
                        BranchId branchId,
                        LocalDateTime from,
                        LocalDateTime to);

        List<AppointmentPersistenceEntity> findByBranchId(BranchId branchId);

        List<AppointmentPersistenceEntity> findByCustomerId(CustomerId customerId);

        List<AppointmentPersistenceEntity> findByVehicleId(com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId vehicleId);

        List<AppointmentPersistenceEntity> findByBranchIdAndStatus(
                        BranchId branchId, AppointmentStatus status);
}