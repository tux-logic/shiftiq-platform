package com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.entities;

import com.tuxlogic.shiftiq.platform.iam.domain.model.entities.WorkshopRoleAuditLog;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workshop_role_audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class WorkshopRoleAuditLogPersistenceEntity {

    @Id
    private UUID id;

    @Column(name = "workshop_id", nullable = false)
    private UUID workshopId;

    @Column(name = "workshop_role_id", nullable = false)
    private UUID workshopRoleId;

    @Column(name = "actor_user_id", nullable = false)
    private UUID actorUserId;

    @Column(name = "target_user_id")
    private UUID targetUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 40)
    private WorkshopRoleAuditLog.ActionType actionType;

    @Column(name = "role_name", nullable = false, length = 80)
    private String roleName;

    @Column(name = "permissions_snapshot", nullable = false, columnDefinition = "TEXT")
    private String permissionsSnapshot;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;
}
