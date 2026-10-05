package com.tuxlogic.shiftiq.platform.iam.domain.model.commands;

/**
 * Revokes the session identified by a refresh token so it can never be exchanged again.
 *
 * @param refreshToken the refresh token presented by the client on sign out
 */
public record RevokeSessionCommand(String refreshToken) {
}
