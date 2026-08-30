package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.iot.domain.model.aggregates.Obd2DeviceRegistration;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.Obd2DeviceRegistrationId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.Obd2RegistrationStatus;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities.Obd2DeviceRegistrationPersistenceEntity;

public class Obd2DeviceRegistrationPersistenceAssembler {

    public static Obd2DeviceRegistrationPersistenceEntity toPersistenceEntity(Obd2DeviceRegistration domain) {
        if (domain == null) {
            return null;
        }
        Obd2DeviceRegistrationPersistenceEntity entity = new Obd2DeviceRegistrationPersistenceEntity();
        entity.setId(domain.getId() != null ? domain.getId().value() : null);
        entity.setObd2DeviceId(domain.getObd2DeviceId());
        entity.setBranchId(domain.getBranchId());
        entity.setVehicleId(domain.getVehicleId());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().value() : null);
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    public static Obd2DeviceRegistration toDomainEntity(Obd2DeviceRegistrationPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Obd2DeviceRegistration(
                new Obd2DeviceRegistrationId(entity.getId()),
                entity.getObd2DeviceId(),
                entity.getBranchId(),
                entity.getVehicleId(),
                entity.getStatus() != null ? new Obd2RegistrationStatus(entity.getStatus()) : null,
                entity.getCreatedAt(),
                entity.getDeletedAt()
        );
    }
}
