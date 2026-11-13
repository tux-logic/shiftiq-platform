package com.tuxlogic.shiftiq.platform.iam.domain.model.entities;

import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Permission;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Immutable audit record for custom workshop role lifecycle and user assignment events.
 */
@Getter
public class WorkshopRoleAuditLog {

    public enum ActionType {
        CREATED,
        UPDATED,
        DEACTIVATED,
        ASSIGNED
    }

    private final UUID id;
    private final UUID workshopId;
    private final UUID workshopRoleId;
    private final UUID actorUserId;
    private final UUID targetUserId;
    private final ActionType actionType;
    private final String roleName;
    private final String permissionsSnapshot;
    private final Instant recordedAt;

    public WorkshopRoleAuditLog(UUID id,
                                UUID workshopId,
                                UUID workshopRoleId,
                                UUID actorUserId,
                                UUID targetUserId,
                                ActionType actionType,
                                String roleName,
                                String permissionsSnapshot,
                                Instant recordedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.workshopId = workshopId;
        this.workshopRoleId = workshopRoleId;
        this.actorUserId = actorUserId;
        this.targetUserId = targetUserId;
        this.actionType = actionType;
        this.roleName = roleName;
        this.permissionsSnapshot = permissionsSnapshot;
        this.recordedAt = recordedAt != null ? recordedAt : Instant.now();
    }

    public static WorkshopRoleAuditLog record(UUID workshopId,
                                              UUID workshopRoleId,
                                              UUID actorUserId,
                                              UUID targetUserId,
                                              ActionType actionType,
                                              String roleName,
                                              Set<Permission> permissions) {
        String snapshot = permissions.stream()
                .map(Permission::name)
                .sorted()
                .collect(Collectors.joining(","));
        return new WorkshopRoleAuditLog(
                UUID.randomUUID(),
                workshopId,
                workshopRoleId,
                actorUserId,
                targetUserId,
                actionType,
                roleName,
                snapshot,
                Instant.now()
        );
    }
}
