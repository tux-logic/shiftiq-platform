package com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.entities.VehiclePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehiclePersistenceRepository extends JpaRepository<VehiclePersistenceEntity, UUID> {

    Optional<VehiclePersistenceEntity> findByVin(String vin);

    Optional<VehiclePersistenceEntity> findByPlateNumber(String plateNumber);
}