package com.tuxlogic.shiftiq.platform.iam.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.WorkshopRole;
import com.tuxlogic.shiftiq.platform.iam.domain.model.entities.WorkshopRoleAuditLog;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Permission;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.WorkshopRoleAuditLogRepository;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.WorkshopRoleRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class WorkshopRoleQueryServiceImpl {

    private final WorkshopRoleRepository workshopRoleRepository;
    private final WorkshopRoleAuditLogRepository auditLogRepository;

    public WorkshopRoleQueryServiceImpl(WorkshopRoleRepository workshopRoleRepository,
                                        WorkshopRoleAuditLogRepository auditLogRepository) {
        this.workshopRoleRepository = workshopRoleRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public List<WorkshopRole> listAvailableForWorkshop(UUID workshopId) {
        return workshopRoleRepository.findAvailableForWorkshop(workshopId);
    }

    public List<WorkshopRoleAuditLog> listAuditLogs(UUID workshopId) {
        return auditLogRepository.findByWorkshopIdOrderByRecordedAtDesc(workshopId);
    }

    public List<Permission> listAssignablePermissions() {
        return Arrays.stream(Permission.values())
                .filter(Permission::isAssignableByWorkshop)
                .toList();
    }
}
