package com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects;

import java.util.EnumSet;
import java.util.Set;

/**
 * Maps built-in {@link Roles} to their default {@link Permission} sets.
 * Used as a zero-config fallback when a user does not have a custom workshop role assigned.
 */
public final class DefaultRolePermissions {

    private static final Set<Permission> ALL_WORKSHOP_PERMISSIONS = EnumSet.of(
            Permission.WORKORDER_READ,
            Permission.WORKORDER_EXECUTE,
            Permission.WORKORDER_DELETE,
            Permission.INVENTORY_READ,
            Permission.INVENTORY_MANAGE,
            Permission.SERVICE_MANAGE,
            Permission.QUOTE_READ,
            Permission.QUOTE_CREATE,
            Permission.QUOTE_APPROVE,
            Permission.QUOTE_CANCEL,
            Permission.VOUCHER_READ,
            Permission.CHECKOUT_PROCESS,
            Permission.APPOINTMENT_MANAGE,
            Permission.STAFF_READ,
            Permission.STAFF_MANAGE,
            Permission.SPECIALTY_MANAGE,
            Permission.BRANCH_READ,
            Permission.BRANCH_MANAGE,
            Permission.MEDIA_UPLOAD,
            Permission.ROLE_MANAGE
    );

    private static final Set<Permission> ASSISTANT_PERMISSIONS = EnumSet.of(
            Permission.WORKORDER_READ,
            Permission.WORKORDER_EXECUTE,
            Permission.INVENTORY_READ,
            Permission.QUOTE_READ,
            Permission.QUOTE_CREATE,
            Permission.QUOTE_APPROVE,
            Permission.VOUCHER_READ,
            Permission.CHECKOUT_PROCESS,
            Permission.APPOINTMENT_MANAGE,
            Permission.STAFF_READ,
            Permission.BRANCH_READ,
            Permission.MEDIA_UPLOAD
    );

    private static final Set<Permission> EMPLOYEE_PERMISSIONS = EnumSet.of(
            Permission.WORKORDER_READ,
            Permission.WORKORDER_EXECUTE,
            Permission.INVENTORY_READ,
            Permission.QUOTE_READ,
            Permission.VOUCHER_READ,
            Permission.STAFF_READ,
            Permission.BRANCH_READ,
            Permission.MEDIA_UPLOAD
    );

    private DefaultRolePermissions() {
    }

    /**
     * Resolves the default immutable permission set for a built-in role.
     *
     * @param role built-in role
     * @return immutable set of permissions
     */
    public static Set<Permission> permissionsFor(Roles role) {
        if (role == null) {
            return Set.of();
        }
        return switch (role) {
            case ROLE_ADMIN -> Set.copyOf(EnumSet.allOf(Permission.class));
            case ROLE_OWNER, ROLE_BRANCH_MANAGER -> Set.copyOf(ALL_WORKSHOP_PERMISSIONS);
            case ROLE_ASSISTANT -> Set.copyOf(ASSISTANT_PERMISSIONS);
            case ROLE_EMPLOYEE -> Set.copyOf(EMPLOYEE_PERMISSIONS);
            case ROLE_USER -> Set.of();
        };
    }

    /**
     * Checks whether a built-in role holds the given permission by default.
     */
    public static boolean hasPermission(Roles role, Permission permission) {
        return permission != null && permissionsFor(role).contains(permission);
    }
}
