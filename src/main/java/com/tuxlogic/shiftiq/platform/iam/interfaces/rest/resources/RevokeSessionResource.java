package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

/**
 * Body of the endpoint that revokes the session identified by a refresh token.
 */
public record RevokeSessionResource(@NotBlank String refreshToken) {
}
