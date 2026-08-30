package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources;

public record ResetPasswordResource(String token, String newPassword) {
}
