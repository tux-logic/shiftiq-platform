package com.tuxlogic.shiftiq.platform.iam.domain.model.commands;

/**
 * Exchanges a refresh token for a new session (new access token and new refresh token).
 *
 * @param refreshToken the refresh token presented by the client
 */
public record RefreshSessionCommand(String refreshToken) {
}
