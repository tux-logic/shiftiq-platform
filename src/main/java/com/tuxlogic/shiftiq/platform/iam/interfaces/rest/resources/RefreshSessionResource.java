package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

/**
 * Body of the endpoint that exchanges a refresh token for a new session.
 */
public record RefreshSessionResource(@NotBlank String refreshToken) {
}
