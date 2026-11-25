package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.BranchBay;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BranchBayId;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.BranchBayPersistenceEntity;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

public class BranchBayPersistenceAssembler {

    public static BranchBay toDomain(BranchBayPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new BranchBay(
                new BranchBayId(entity.getId()),
                new BranchId(entity.getBranchId()),
                entity.getCode(),
                entity.getName(),
                entity.getBayType(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getVersion()
        );
    }

    public static void toEntity(BranchBay domain, BranchBayPersistenceEntity entity) {
        if (domain.getVersion() != null) {
            entity.setId(domain.getId() != null ? domain.getId().value() : null);
            entity.setCreatedAt(domain.getCreatedAt());
            entity.setUpdatedAt(domain.getUpdatedAt());
            entity.setVersion(domain.getVersion());
        }
        if (domain.getBranchId() != null) {
            entity.setBranchId(domain.getBranchId().value());
        }
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setBayType(domain.getType());
        entity.setActive(domain.isActive());
        entity.setDeletedAt(domain.getDeletedAt());
    }
}