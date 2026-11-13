package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources;

import com.tuxlogic.shiftiq.platform.iam.domain.model.entities.WorkshopRoleAuditLog;

import java.time.Instant;
import java.util.UUID;

public record WorkshopRoleAuditLogResource(
        UUID id,
        UUID workshopId,
        UUID workshopRoleId,
        UUID actorUserId,
        UUID targetUserId,
        String actionType,
        String roleName,
        String permissionsSnapshot,
        Instant recordedAt
) {
    public static WorkshopRoleAuditLogResource from(WorkshopRoleAuditLog log) {
        return new WorkshopRoleAuditLogResource(
                log.getId(),
                log.getWorkshopId(),
                log.getWorkshopRoleId(),
                log.getActorUserId(),
                log.getTargetUserId(),
                log.getActionType().name(),
                log.getRoleName(),
                log.getPermissionsSnapshot(),
                log.getRecordedAt()
        );
    }
}
