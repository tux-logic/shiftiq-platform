package com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.WorkshopRole;
import com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.entities.WorkshopRolePersistenceEntity;

import java.util.HashSet;

public final class WorkshopRolePersistenceAssembler {

    private WorkshopRolePersistenceAssembler() {
    }

    public static WorkshopRolePersistenceEntity toEntity(WorkshopRole domain) {
        var entity = new WorkshopRolePersistenceEntity();
        entity.setId(domain.getId());
        entity.setWorkshopId(domain.getWorkshopId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setSystem(domain.isSystem());
        entity.setActive(domain.isActive());
        entity.setPermissions(new HashSet<>(domain.getPermissions()));
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public static WorkshopRole toDomain(WorkshopRolePersistenceEntity entity) {
        return new WorkshopRole(
                entity.getId(),
                entity.getWorkshopId(),
                entity.getName(),
                entity.getDescription(),
                entity.isSystem(),
                entity.isActive(),
                entity.getPermissions(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
