package com.tuxlogic.shiftiq.platform.iam.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.User;
import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.WorkshopRole;
import com.tuxlogic.shiftiq.platform.iam.domain.model.entities.WorkshopRoleAuditLog;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Permission;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.UserId;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.UserRepository;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.WorkshopRoleAuditLogRepository;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.WorkshopRoleRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
public class WorkshopRoleCommandServiceImpl {

    private final WorkshopRoleRepository workshopRoleRepository;
    private final WorkshopRoleAuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public WorkshopRoleCommandServiceImpl(WorkshopRoleRepository workshopRoleRepository,
                                          WorkshopRoleAuditLogRepository auditLogRepository,
                                          UserRepository userRepository) {
        this.workshopRoleRepository = workshopRoleRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public WorkshopRole createCustomRole(UUID workshopId,
                                         UUID actorUserId,
                                         Set<Permission> actorPermissions,
                                         boolean actorIsAdmin,
                                         String name,
                                         String description,
                                         Set<Permission> requestedPermissions) {
        validatePermissionsSubset(requestedPermissions, actorPermissions, actorIsAdmin);
        workshopRoleRepository.findByWorkshopIdAndNameIgnoreCase(workshopId, name != null ? name.trim() : "")
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("iam.error.workshopRole.duplicateName");
                });

        WorkshopRole role = WorkshopRole.createCustom(workshopId, name, description, requestedPermissions);
        WorkshopRole saved = workshopRoleRepository.save(role);
        auditLogRepository.save(WorkshopRoleAuditLog.record(
                workshopId,
                saved.getId(),
                actorUserId,
                null,
                WorkshopRoleAuditLog.ActionType.CREATED,
                saved.getName(),
                saved.getPermissions()
        ));
        return saved;
    }

    @Transactional
    public WorkshopRole updateCustomRole(UUID workshopId,
                                         UUID roleId,
                                         UUID actorUserId,
                                         Set<Permission> actorPermissions,
                                         boolean actorIsAdmin,
                                         String name,
                                         String description,
                                         Set<Permission> requestedPermissions) {
        WorkshopRole role = workshopRoleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("iam.error.workshopRole.notFound"));
        verifyMutableWorkshopRole(workshopId, role);
        validatePermissionsSubset(requestedPermissions, actorPermissions, actorIsAdmin);

        workshopRoleRepository.findByWorkshopIdAndNameIgnoreCase(workshopId, name != null ? name.trim() : "")
                .filter(existing -> !existing.getId().equals(roleId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("iam.error.workshopRole.duplicateName");
                });

        role.update(name, description, requestedPermissions);
        WorkshopRole saved = workshopRoleRepository.save(role);
        auditLogRepository.save(WorkshopRoleAuditLog.record(
                workshopId,
                saved.getId(),
                actorUserId,
                null,
                WorkshopRoleAuditLog.ActionType.UPDATED,
                saved.getName(),
                saved.getPermissions()
        ));
        return saved;
    }

    @Transactional
    public void deactivateCustomRole(UUID workshopId, UUID roleId, UUID actorUserId) {
        WorkshopRole role = workshopRoleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("iam.error.workshopRole.notFound"));
        verifyMutableWorkshopRole(workshopId, role);

        role.deactivate();
        WorkshopRole saved = workshopRoleRepository.save(role);
        auditLogRepository.save(WorkshopRoleAuditLog.record(
                workshopId,
                saved.getId(),
                actorUserId,
                null,
                WorkshopRoleAuditLog.ActionType.DEACTIVATED,
                saved.getName(),
                saved.getPermissions()
        ));
    }

    @Transactional
    public void assignRoleToUser(UUID workshopId,
                                 UUID roleId,
                                 UUID targetUserId,
                                 UUID actorUserId,
                                 Set<Permission> actorPermissions,
                                 boolean actorIsAdmin) {
        WorkshopRole role = workshopRoleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("iam.error.workshopRole.notFound"));
        if (!role.isActive()) {
            throw new IllegalArgumentException("iam.error.workshopRole.inactive");
        }
        if (!role.isSystem() && !workshopId.equals(role.getWorkshopId())) {
            throw new AccessDeniedException("Cross-tenant workshop role assignment is forbidden");
        }
        validatePermissionsSubset(role.getPermissions(), actorPermissions, actorIsAdmin);

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("iam.error.user.notFound"));
        targetUser.assignWorkshopRole(role.getId());
        userRepository.save(targetUser);

        auditLogRepository.save(WorkshopRoleAuditLog.record(
                workshopId,
                role.getId(),
                actorUserId,
                targetUserId,
                WorkshopRoleAuditLog.ActionType.ASSIGNED,
                role.getName(),
                role.getPermissions()
        ));
    }

    private static void verifyMutableWorkshopRole(UUID workshopId, WorkshopRole role) {
        if (role.isSystem()) {
            throw new AccessDeniedException("System role templates are immutable");
        }
        if (!workshopId.equals(role.getWorkshopId())) {
            throw new AccessDeniedException("Cross-tenant workshop role access is forbidden");
        }
    }

    private static void validatePermissionsSubset(Set<Permission> requested,
                                                  Set<Permission> actorPermissions,
                                                  boolean actorIsAdmin) {
        if (requested == null || requested.isEmpty()) {
            throw new IllegalArgumentException("iam.error.workshopRole.permissionsRequired");
        }
        if (requested.contains(Permission.PLATFORM_ADMIN)) {
            throw new AccessDeniedException("PLATFORM_ADMIN permission cannot be assigned to workshop roles");
        }
        if (!actorIsAdmin && (actorPermissions == null || !actorPermissions.containsAll(requested))) {
            throw new AccessDeniedException("Cannot grant permissions you do not possess");
        }
    }
}
