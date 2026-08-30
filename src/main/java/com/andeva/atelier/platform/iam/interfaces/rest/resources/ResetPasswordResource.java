package com.andeva.atelier.platform.iam.interfaces.rest.resources;

public record ResetPasswordResource(String token, String newPassword) {
}
