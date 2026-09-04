package com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.AppointmentStatus;
import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.entities.AppointmentPersistenceEntity;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AppointmentJpaRepository extends JpaRepository<AppointmentPersistenceEntity, UUID> {

        boolean existsByBranchIdAndVehicleIdAndScheduledStartLessThanAndScheduledEndGreaterThan(
                        BranchId branchId, VehicleId vehicleId,
                        LocalDateTime scheduledEnd, LocalDateTime scheduledStart);

        boolean existsByIdNotAndBranchIdAndVehicleIdAndScheduledStartLessThanAndScheduledEndGreaterThan(
                        UUID appointmentId, BranchId branchId, VehicleId vehicleId, 
                        LocalDateTime scheduledEnd, LocalDateTime scheduledStart);

        Page<AppointmentPersistenceEntity> findByBranchId(BranchId branchId, Pageable pageable);
        Page<AppointmentPersistenceEntity> findByCustomerId(CustomerId customerId, Pageable pageable);
        Page<AppointmentPersistenceEntity> findByVehicleId(VehicleId vehicleId, Pageable pageable);
        Page<AppointmentPersistenceEntity> findByBranchIdAndStatus(BranchId branchId, AppointmentStatus status, Pageable pageable);
}
