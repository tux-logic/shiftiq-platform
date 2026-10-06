package com.tuxlogic.shiftiq.platform.iam.domain.model.queries;

import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.User;

/**
 * Result of a successful authentication: the user plus the session tokens.
 *
 * @param user                        the authenticated user
 * @param token                       the short lived access token sent as {@code Authorization: Bearer ...}
 * @param refreshToken                the long lived token exchanged for a new session on the refresh endpoint
 * @param accessTokenExpiresInSeconds lifetime of {@code token}, in seconds
 */
public record AuthenticatedUser(User user, String token, String refreshToken, long accessTokenExpiresInSeconds) {
}
