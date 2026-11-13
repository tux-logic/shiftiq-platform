package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources;

import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.WorkshopRole;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Permission;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record WorkshopRoleResource(
        UUID id,
        UUID workshopId,
        String name,
        String description,
        boolean system,
        boolean active,
        Set<String> permissions
) {
    public static WorkshopRoleResource from(WorkshopRole role) {
        return new WorkshopRoleResource(
                role.getId(),
                role.getWorkshopId(),
                role.getName(),
                role.getDescription(),
                role.isSystem(),
                role.isActive(),
                role.getPermissions().stream().map(Permission::name).collect(Collectors.toUnmodifiableSet())
        );
    }
}
