package com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionCatalogTest {

    @Test
    @DisplayName("ROLE_ADMIN holds all permissions including PLATFORM_ADMIN")
    void adminHoldsAllPermissions() {
        var permissions = DefaultRolePermissions.permissionsFor(Roles.ROLE_ADMIN);
        assertThat(permissions).containsAll(java.util.List.of(Permission.values()));
    }

    @Test
    @DisplayName("ROLE_OWNER and ROLE_BRANCH_MANAGER hold all workshop permissions except PLATFORM_ADMIN")
    void ownerAndManagerHoldWorkshopPermissions() {
        for (Roles role : new Roles[]{Roles.ROLE_OWNER, Roles.ROLE_BRANCH_MANAGER}) {
            var permissions = DefaultRolePermissions.permissionsFor(role);
            assertThat(permissions)
                    .contains(Permission.BRANCH_MANAGE, Permission.ROLE_MANAGE, Permission.QUOTE_CANCEL, Permission.STAFF_MANAGE)
                    .doesNotContain(Permission.PLATFORM_ADMIN);
        }
    }

    @Test
    @DisplayName("ROLE_ASSISTANT holds advisor and billing permissions but not managerial or platform permissions")
    void assistantHoldsAdvisorPermissions() {
        var permissions = DefaultRolePermissions.permissionsFor(Roles.ROLE_ASSISTANT);
        assertThat(permissions)
                .contains(Permission.QUOTE_CREATE, Permission.QUOTE_APPROVE, Permission.CHECKOUT_PROCESS, Permission.APPOINTMENT_MANAGE)
                .doesNotContain(Permission.QUOTE_CANCEL, Permission.BRANCH_MANAGE, Permission.STAFF_MANAGE, Permission.ROLE_MANAGE, Permission.PLATFORM_ADMIN);
    }

    @Test
    @DisplayName("ROLE_EMPLOYEE holds operational workorder permissions without billing or management")
    void employeeHoldsOperationalPermissionsOnly() {
        var permissions = DefaultRolePermissions.permissionsFor(Roles.ROLE_EMPLOYEE);
        assertThat(permissions)
                .contains(Permission.WORKORDER_READ, Permission.WORKORDER_EXECUTE, Permission.MEDIA_UPLOAD)
                .doesNotContain(Permission.QUOTE_CREATE, Permission.CHECKOUT_PROCESS, Permission.BRANCH_MANAGE, Permission.INVENTORY_MANAGE, Permission.STAFF_MANAGE);
    }

    @Test
    @DisplayName("ROLE_USER holds no internal staff permissions")
    void userHoldsNoStaffPermissions() {
        assertThat(DefaultRolePermissions.permissionsFor(Roles.ROLE_USER)).isEmpty();
        assertThat(DefaultRolePermissions.permissionsFor(null)).isEmpty();
    }

    @Test
    @DisplayName("PLATFORM_ADMIN is marked as not assignable by workshops")
    void platformAdminIsNotAssignableByWorkshops() {
        assertThat(Permission.PLATFORM_ADMIN.isAssignableByWorkshop()).isFalse();
        assertThat(Permission.INVENTORY_MANAGE.isAssignableByWorkshop()).isTrue();
    }
}
