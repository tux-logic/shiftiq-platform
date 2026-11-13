package com.tuxlogic.shiftiq.platform.iam.domain.repositories;

import com.tuxlogic.shiftiq.platform.iam.domain.model.entities.WorkshopRoleAuditLog;

import java.util.List;
import java.util.UUID;

public interface WorkshopRoleAuditLogRepository {
    WorkshopRoleAuditLog save(WorkshopRoleAuditLog log);
    List<WorkshopRoleAuditLog> findByWorkshopIdOrderByRecordedAtDesc(UUID workshopId);
}
