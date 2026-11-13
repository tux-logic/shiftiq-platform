package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources;

import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Permission;

public record PermissionResource(String code, String description) {
    public static PermissionResource from(Permission permission) {
        return new PermissionResource(permission.name(), permission.getDescription());
    }
}
