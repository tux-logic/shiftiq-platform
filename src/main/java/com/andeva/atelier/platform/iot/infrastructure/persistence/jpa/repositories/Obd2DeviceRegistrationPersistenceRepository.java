package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.iot.domain.model.valueobjects.Obd2DeviceId;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities.Obd2DeviceRegistrationPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for "obd2_device_registrations".
 */
@Repository
public interface Obd2DeviceRegistrationPersistenceRepository extends JpaRepository<Obd2DeviceRegistrationPersistenceEntity, UUID> {

    Optional<Obd2DeviceRegistrationPersistenceEntity> findByObd2DeviceIdAndStatus(Obd2DeviceId obd2DeviceId, String status);

    Optional<Obd2DeviceRegistrationPersistenceEntity> findByVehicleIdAndStatus(VehicleId vehicleId, String status);

    List<Obd2DeviceRegistrationPersistenceEntity> findAllByBranchIdAndStatus(BranchId branchId, String status);
}
