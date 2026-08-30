package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities.VehicleRegistrationPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for "vehicle_registrations".
 */
@Repository
public interface VehicleRegistrationPersistenceRepository extends JpaRepository<VehicleRegistrationPersistenceEntity, UUID> {

    /**
     * Finds the registration for a vehicle by its status.
     * @param vehicleId the vehicle ID value object
     * @param status the status string
     * @return an Optional containing the registration if found
     */
    Optional<VehicleRegistrationPersistenceEntity> findByVehicleIdAndStatus(VehicleId vehicleId, String status);

    /**
     * Finds all registrations for a user by their status.
     * @param userId the user ID UUID
     * @param status the status string
     * @return the list of registrations
     */
    List<VehicleRegistrationPersistenceEntity> findAllByUserIdAndStatus(UUID userId, String status);
}
