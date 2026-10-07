package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.WorkshopSpecialty;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopSpecialtyId;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.WorkshopSpecialtyPersistenceEntity;

public class WorkshopSpecialtyPersistenceAssembler {

    public static WorkshopSpecialty toDomain(WorkshopSpecialtyPersistenceEntity entity) {
        if (entity == null) return null;
        return new WorkshopSpecialty(
                new WorkshopSpecialtyId(entity.getId()),
                new WorkshopId(entity.getWorkshopId()),
                entity.getName(),
                entity.getCode(),
                entity.getDescription(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getVersion()
        );
    }

    public static void toEntity(WorkshopSpecialty domain, WorkshopSpecialtyPersistenceEntity entity) {
        if (domain.getId() != null) {
            entity.setId(domain.getId().value());
        }
        if (domain.getWorkshopId() != null) {
            entity.setWorkshopId(domain.getWorkshopId().value());
        }
        entity.setName(domain.getName());
        entity.setCode(domain.getCode());
        entity.setDescription(domain.getDescription());
        entity.setActive(domain.isActive());
        entity.setDeletedAt(domain.getDeletedAt());
    }
}
