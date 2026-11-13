package com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates;

import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Permission;
import lombok.Getter;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * WorkshopRole aggregate representing either an immutable system role template (workshopId == null)
 * or a workshop-scoped custom role composed from the fixed {@link Permission} catalog.
 */
@Getter
public class WorkshopRole {

    private final UUID id;
    private final UUID workshopId;
    private String name;
    private String description;
    private final boolean system;
    private boolean active;
    private Set<Permission> permissions;
    private final Instant createdAt;
    private Instant updatedAt;

    public WorkshopRole(UUID id,
                        UUID workshopId,
                        String name,
                        String description,
                        boolean system,
                        boolean active,
                        Set<Permission> permissions,
                        Instant createdAt,
                        Instant updatedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.workshopId = workshopId;
        this.name = validateName(name);
        this.description = description != null ? description.trim() : null;
        this.system = system;
        this.active = active;
        this.permissions = validatePermissions(permissions);
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static WorkshopRole createCustom(UUID workshopId,
                                            String name,
                                            String description,
                                            Set<Permission> permissions) {
        if (workshopId == null) {
            throw new IllegalArgumentException("iam.error.workshopRole.workshopIdRequired");
        }
        Instant now = Instant.now();
        return new WorkshopRole(UUID.randomUUID(), workshopId, name, description, false, true, permissions, now, now);
    }

    public void update(String name, String description, Set<Permission> permissions) {
        if (this.system) {
            throw new IllegalStateException("iam.error.workshopRole.systemImmutable");
        }
        this.name = validateName(name);
        this.description = description != null ? description.trim() : null;
        this.permissions = validatePermissions(permissions);
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        if (this.system) {
            throw new IllegalStateException("iam.error.workshopRole.systemImmutable");
        }
        this.active = false;
        this.updatedAt = Instant.now();
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("iam.error.workshopRole.nameRequired");
        }
        String trimmed = name.trim();
        if (trimmed.length() > 80) {
            throw new IllegalArgumentException("iam.error.workshopRole.nameTooLong");
        }
        return trimmed;
    }

    private static Set<Permission> validatePermissions(Set<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            throw new IllegalArgumentException("iam.error.workshopRole.permissionsRequired");
        }
        EnumSet<Permission> validated = EnumSet.copyOf(permissions);
        if (validated.contains(Permission.PLATFORM_ADMIN)) {
            throw new IllegalArgumentException("iam.error.workshopRole.platformAdminForbidden");
        }
        return validated;
    }
}
