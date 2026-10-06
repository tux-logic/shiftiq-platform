package com.tuxlogic.shiftiq.platform.iam.application.internal.services;

import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.tokens.TokenService;
import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.User;
import com.tuxlogic.shiftiq.platform.iam.domain.model.entities.RefreshToken;
import com.tuxlogic.shiftiq.platform.iam.domain.model.queries.AuthenticatedUser;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Issues a session for a user: a short lived access token plus a single use
 * refresh token persisted by its SHA-256 hash.
 *
 * <p>Shared by sign-in and by the refresh endpoint so both flows always agree on
 * token lifetimes and on how refresh tokens are stored.</p>
 */
@Component
public class SessionIssuer {

    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final int accessTokenExpirationMinutes;
    private final int refreshTokenExpirationDays;

    public SessionIssuer(TokenService tokenService,
                         RefreshTokenRepository refreshTokenRepository,
                         @Value("${authorization.jwt.access-token.expiration.minutes:15}") int accessTokenExpirationMinutes,
                         @Value("${authorization.jwt.refresh-token.expiration.days:7}") int refreshTokenExpirationDays) {
        this.tokenService = tokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
    }

    public AuthenticatedUser issue(User user) {
        var accessToken = tokenService.generateToken(user.getEmail().value());
        var refreshToken = tokenService.generateRefreshToken(user.getEmail().value());
        refreshTokenRepository.save(new RefreshToken(
                hashToken(refreshToken),
                user.getId().value(),
                refreshTokenExpirationDays
        ));
        return new AuthenticatedUser(user, accessToken, refreshToken, accessTokenExpirationMinutes * 60L);
    }

    /**
     * Stores the token by its SHA-256 hash: the raw token is never persisted.
     */
    public String hashToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
