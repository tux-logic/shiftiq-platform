package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources;

public record UpdateUserPasswordResource(
        String currentPassword,
        String newPassword
) {
}
