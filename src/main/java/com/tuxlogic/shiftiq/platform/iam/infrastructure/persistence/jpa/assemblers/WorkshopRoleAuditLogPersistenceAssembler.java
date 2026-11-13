package com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.iam.domain.model.entities.WorkshopRoleAuditLog;
import com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.entities.WorkshopRoleAuditLogPersistenceEntity;

public final class WorkshopRoleAuditLogPersistenceAssembler {

    private WorkshopRoleAuditLogPersistenceAssembler() {
    }

    public static WorkshopRoleAuditLogPersistenceEntity toEntity(WorkshopRoleAuditLog domain) {
        var entity = new WorkshopRoleAuditLogPersistenceEntity();
        entity.setId(domain.getId());
        entity.setWorkshopId(domain.getWorkshopId());
        entity.setWorkshopRoleId(domain.getWorkshopRoleId());
        entity.setActorUserId(domain.getActorUserId());
        entity.setTargetUserId(domain.getTargetUserId());
        entity.setActionType(domain.getActionType());
        entity.setRoleName(domain.getRoleName());
        entity.setPermissionsSnapshot(domain.getPermissionsSnapshot());
        entity.setRecordedAt(domain.getRecordedAt());
        return entity;
    }

    public static WorkshopRoleAuditLog toDomain(WorkshopRoleAuditLogPersistenceEntity entity) {
        return new WorkshopRoleAuditLog(
                entity.getId(),
                entity.getWorkshopId(),
                entity.getWorkshopRoleId(),
                entity.getActorUserId(),
                entity.getTargetUserId(),
                entity.getActionType(),
                entity.getRoleName(),
                entity.getPermissionsSnapshot(),
                entity.getRecordedAt()
        );
    }
}
