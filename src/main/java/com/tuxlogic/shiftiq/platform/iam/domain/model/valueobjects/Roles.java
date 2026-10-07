package com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects;

/**
 * Value object enum representing user roles in the system.
 */
public enum Roles {
    ROLE_USER,
    ROLE_ADMIN,
    ROLE_EMPLOYEE,
    ROLE_OWNER,
    ROLE_BRANCH_MANAGER,
    ROLE_ASSISTANT;

    /**
     * Relative privilege level used to prevent privilege downgrades when a staff
     * registration tries to (re)assign a role to an existing account.
     *
     * @return the rank of this role, higher means more privileged
     */
    public int hierarchyRank() {
        return switch (this) {
            case ROLE_USER, ROLE_EMPLOYEE -> 0;
            case ROLE_ASSISTANT -> 1;
            case ROLE_BRANCH_MANAGER -> 2;
            case ROLE_OWNER -> 3;
            case ROLE_ADMIN -> 4;
        };
    }

    /**
     * @param other role that would replace this one
     * @return {@code true} when assigning {@code other} does not reduce privileges
     */
    public boolean canBeReplacedBy(Roles other) {
        return other != null && other.hierarchyRank() >= this.hierarchyRank();
    }

    /**
     * @param roleName raw role name, with or without the {@code ROLE_} prefix
     * @return the matching role, {@code null} when the name is unknown
     */
    public static Roles fromName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return null;
        }
        String normalized = roleName.toUpperCase().trim();
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        for (Roles role : values()) {
            if (role.name().equals(normalized)) {
                return role;
            }
        }
        return null;
    }
}
