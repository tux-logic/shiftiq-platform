package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleRegistration;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.VehicleRegistrationId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.VehicleRegistrationStatus;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities.VehicleRegistrationPersistenceEntity;

/**
 * Assembler class to transform between VehicleRegistration persistence entities and domain aggregates.
 */
public class VehicleRegistrationPersistenceAssembler {

    public static VehicleRegistrationPersistenceEntity toPersistenceEntity(VehicleRegistration domain) {
        if (domain == null) {
            return null;
        }
        VehicleRegistrationPersistenceEntity entity = new VehicleRegistrationPersistenceEntity();
        entity.setId(domain.getId() != null ? domain.getId().value() : null);
        entity.setUserId(domain.getUserId());
        entity.setVehicleId(domain.getVehicleId());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().value() : null);
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    public static VehicleRegistration toDomainEntity(VehicleRegistrationPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new VehicleRegistration(
                new VehicleRegistrationId(entity.getId()),
                entity.getUserId(),
                entity.getVehicleId(),
                entity.getStatus() != null ? new VehicleRegistrationStatus(entity.getStatus()) : null,
                entity.getCreatedAt(),
                entity.getDeletedAt()
        );
    }
}
