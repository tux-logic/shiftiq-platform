package com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates;

import com.tuxlogic.shiftiq.platform.iam.domain.model.entities.WorkshopRoleAuditLog;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Permission;
import com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.assemblers.WorkshopRolePersistenceAssembler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkshopRoleTest {

    @Test
    @DisplayName("Custom WorkshopRole can be created, updated, deactivated, and mapped to persistence entity")
    void customWorkshopRoleLifecycle() {
        UUID workshopId = UUID.randomUUID();
        WorkshopRole role = WorkshopRole.createCustom(
                workshopId,
                "Warehouse Manager",
                "Manages stock and product batches",
                Set.of(Permission.INVENTORY_READ, Permission.INVENTORY_MANAGE)
        );

        assertThat(role.getWorkshopId()).isEqualTo(workshopId);
        assertThat(role.isSystem()).isFalse();
        assertThat(role.isActive()).isTrue();
        assertThat(role.getPermissions()).containsExactlyInAnyOrder(Permission.INVENTORY_READ, Permission.INVENTORY_MANAGE);

        role.update("Senior Warehouse", "Updated desc", Set.of(Permission.INVENTORY_READ, Permission.INVENTORY_MANAGE, Permission.BRANCH_READ));
        assertThat(role.getName()).isEqualTo("Senior Warehouse");
        assertThat(role.getPermissions()).contains(Permission.BRANCH_READ);

        var entity = WorkshopRolePersistenceAssembler.toEntity(role);
        var mapped = WorkshopRolePersistenceAssembler.toDomain(entity);
        assertThat(mapped.getId()).isEqualTo(role.getId());
        assertThat(mapped.getPermissions()).containsExactlyInAnyOrderElementsOf(role.getPermissions());

        role.deactivate();
        assertThat(role.isActive()).isFalse();
    }

    @Test
    @DisplayName("System WorkshopRole templates are immutable")
    void systemRoleIsImmutable() {
        WorkshopRole systemRole = new WorkshopRole(
                UUID.randomUUID(),
                null,
                "ROLE_OWNER",
                "System owner",
                true,
                true,
                Set.of(Permission.BRANCH_MANAGE),
                Instant.now(),
                Instant.now()
        );

        assertThrows(IllegalStateException.class, () -> systemRole.update("Hacked", null, Set.of(Permission.BRANCH_MANAGE)));
        assertThrows(IllegalStateException.class, systemRole::deactivate);
    }

    @Test
    @DisplayName("WorkshopRole rejects PLATFORM_ADMIN permission and blank names")
    void workshopRoleRejectsPlatformAdminAndBlankName() {
        UUID workshopId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> WorkshopRole.createCustom(
                workshopId, "Bad", null, Set.of(Permission.PLATFORM_ADMIN)));
        assertThrows(IllegalArgumentException.class, () -> WorkshopRole.createCustom(
                workshopId, "   ", null, Set.of(Permission.INVENTORY_READ)));
        assertThrows(IllegalArgumentException.class, () -> WorkshopRole.createCustom(
                workshopId, "Empty", null, Set.of()));
    }

    @Test
    @DisplayName("WorkshopRoleAuditLog records sorted permission snapshot")
    void auditLogRecordsSnapshot() {
        var log = WorkshopRoleAuditLog.record(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                WorkshopRoleAuditLog.ActionType.CREATED,
                "Cashier",
                Set.of(Permission.VOUCHER_READ, Permission.CHECKOUT_PROCESS)
        );

        assertThat(log.getPermissionsSnapshot()).isEqualTo("CHECKOUT_PROCESS,VOUCHER_READ");
        assertThat(log.getActionType()).isEqualTo(WorkshopRoleAuditLog.ActionType.CREATED);
    }
}
