package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.operations.domain.model.aggregates.Service;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.ServiceId;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.ServicePersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;

public final class ServicePersistenceAssembler {

    private ServicePersistenceAssembler() {}

    public static ServicePersistenceEntity toEntity(Service service, ServicePersistenceEntity entity) {
        if (entity == null) {
            entity = new ServicePersistenceEntity();
        }
        if (service.getVersion() != null) {
            entity.setId(service.getId() != null ? service.getId().value() : null);
        }
        entity.setBranchId(service.getBranchId() != null ? service.getBranchId().value() : null);
        entity.setName(service.getName());
        entity.setPrice(service.getPrice());
        entity.setCreatedAt(service.getCreatedAt());
        entity.setUpdatedAt(service.getUpdatedAt());
        entity.setDeletedAt(service.getDeletedAt());
        entity.setCreatedBy(service.getCreatedBy());
        entity.setUpdatedBy(service.getUpdatedBy());
        entity.setVersion(service.getVersion());
        return entity;
    }

    public static Service toDomain(ServicePersistenceEntity entity) {
        return new Service(
                new ServiceId(entity.getId()),
                new BranchId(entity.getBranchId()),
                entity.getName(),
                entity.getPrice(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedBy(),
                entity.getVersion()
        );
    }
}

