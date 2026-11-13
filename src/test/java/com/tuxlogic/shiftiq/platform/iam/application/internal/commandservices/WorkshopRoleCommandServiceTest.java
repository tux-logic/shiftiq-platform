package com.tuxlogic.shiftiq.platform.iam.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.User;
import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.WorkshopRole;
import com.tuxlogic.shiftiq.platform.iam.domain.model.entities.WorkshopRoleAuditLog;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.*;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.UserRepository;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.WorkshopRoleAuditLogRepository;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.WorkshopRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkshopRoleCommandServiceTest {

    @Mock
    private WorkshopRoleRepository workshopRoleRepository;

    @Mock
    private WorkshopRoleAuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    private WorkshopRoleCommandServiceImpl service;
    private final UUID workshopId = UUID.randomUUID();
    private final UUID otherWorkshopId = UUID.randomUUID();
    private final UUID actorId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new WorkshopRoleCommandServiceImpl(workshopRoleRepository, auditLogRepository, userRepository);
    }

    @Test
    @DisplayName("createCustomRole persists custom role and records CREATED audit log")
    void createCustomRoleSucceedsAndAudits() {
        when(workshopRoleRepository.findByWorkshopIdAndNameIgnoreCase(workshopId, "Cashier")).thenReturn(Optional.empty());
        when(workshopRoleRepository.save(any(WorkshopRole.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditLogRepository.save(any(WorkshopRoleAuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkshopRole created = service.createCustomRole(
                workshopId,
                actorId,
                Set.of(Permission.VOUCHER_READ, Permission.CHECKOUT_PROCESS, Permission.ROLE_MANAGE),
                false,
                "Cashier",
                "Billing cashier",
                Set.of(Permission.VOUCHER_READ, Permission.CHECKOUT_PROCESS)
        );

        assertThat(created.getName()).isEqualTo("Cashier");
        assertThat(created.getPermissions()).containsExactlyInAnyOrder(Permission.VOUCHER_READ, Permission.CHECKOUT_PROCESS);
        verify(auditLogRepository).save(argThat(log -> log.getActionType() == WorkshopRoleAuditLog.ActionType.CREATED));
    }

    @Test
    @DisplayName("createCustomRole rejects privilege escalation when caller lacks requested permission")
    void createCustomRoleRejectsPrivilegeEscalation() {
        assertThrows(AccessDeniedException.class, () -> service.createCustomRole(
                workshopId,
                actorId,
                Set.of(Permission.ROLE_MANAGE, Permission.INVENTORY_READ),
                false,
                "Super",
                null,
                Set.of(Permission.INVENTORY_READ, Permission.BRANCH_MANAGE)
        ));

        assertThrows(AccessDeniedException.class, () -> service.createCustomRole(
                workshopId,
                actorId,
                Set.of(Permission.PLATFORM_ADMIN),
                true,
                "AdminClone",
                null,
                Set.of(Permission.PLATFORM_ADMIN)
        ));
        verify(workshopRoleRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateCustomRole and deactivateCustomRole deny system roles and cross-tenant roles")
    void updateAndDeactivateDenySystemAndCrossTenantRoles() {
        UUID systemRoleId = UUID.randomUUID();
        WorkshopRole systemRole = new WorkshopRole(
                systemRoleId, null, "ROLE_OWNER", "Owner", true, true,
                Set.of(Permission.BRANCH_MANAGE), Instant.now(), Instant.now());
        when(workshopRoleRepository.findById(systemRoleId)).thenReturn(Optional.of(systemRole));

        assertThrows(AccessDeniedException.class, () -> service.updateCustomRole(
                workshopId, systemRoleId, actorId, Set.of(Permission.BRANCH_MANAGE), false,
                "Renamed", null, Set.of(Permission.BRANCH_MANAGE)));
        assertThrows(AccessDeniedException.class, () -> service.deactivateCustomRole(workshopId, systemRoleId, actorId));

        UUID foreignRoleId = UUID.randomUUID();
        WorkshopRole foreignRole = WorkshopRole.createCustom(otherWorkshopId, "Foreign", null, Set.of(Permission.BRANCH_READ));
        when(workshopRoleRepository.findById(foreignRoleId)).thenReturn(Optional.of(foreignRole));

        assertThrows(AccessDeniedException.class, () -> service.deactivateCustomRole(workshopId, foreignRoleId, actorId));
    }

    @Test
    @DisplayName("assignRoleToUser assigns role to user and writes ASSIGNED audit log")
    void assignRoleToUserSucceeds() {
        UUID targetUserId = UUID.randomUUID();
        WorkshopRole customRole = WorkshopRole.createCustom(workshopId, "Warehouse", null, Set.of(Permission.INVENTORY_MANAGE));
        User targetUser = new User(new UserId(targetUserId), new EmailAddress("w@test.com"), new Password("hash"),
                null, UserStatus.ACTIVE, Roles.ROLE_EMPLOYEE, Set.of(), Instant.now(), Instant.now(), null, 0L);

        when(workshopRoleRepository.findById(customRole.getId())).thenReturn(Optional.of(customRole));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        when(auditLogRepository.save(any(WorkshopRoleAuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        service.assignRoleToUser(
                workshopId,
                customRole.getId(),
                targetUserId,
                actorId,
                Set.of(Permission.INVENTORY_MANAGE, Permission.ROLE_MANAGE),
                false
        );

        assertThat(targetUser.getWorkshopRoleId()).isEqualTo(customRole.getId());
        verify(auditLogRepository).save(argThat(log ->
                log.getActionType() == WorkshopRoleAuditLog.ActionType.ASSIGNED
                        && targetUserId.equals(log.getTargetUserId())));
    }
}
