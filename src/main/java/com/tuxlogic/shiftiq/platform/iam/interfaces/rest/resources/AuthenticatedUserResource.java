package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources;

import java.util.UUID;

/**
 * Authentication response.
 *
 * @param id                          the user identifier
 * @param email                       the user email
 * @param role                        the user role
 * @param token                       the short lived access token (Authorization: Bearer ...)
 * @param refreshToken                the token to post to the refresh endpoint when the access token expires
 * @param accessTokenExpiresInSeconds lifetime of {@code token}, in seconds
 */
public record AuthenticatedUserResource(UUID id, String email, String role, String token,
                                        String refreshToken, long accessTokenExpiresInSeconds) {
}
