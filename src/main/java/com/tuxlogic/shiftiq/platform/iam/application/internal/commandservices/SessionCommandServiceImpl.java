package com.tuxlogic.shiftiq.platform.iam.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.iam.application.commandservices.SessionCommandService;
import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.tokens.TokenService;
import com.tuxlogic.shiftiq.platform.iam.application.internal.services.SessionIssuer;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.RefreshSessionCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.RevokeSessionCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.queries.AuthenticatedUser;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.RefreshTokenRepository;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class SessionCommandServiceImpl implements SessionCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionCommandServiceImpl.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final SessionIssuer sessionIssuer;

    public SessionCommandServiceImpl(RefreshTokenRepository refreshTokenRepository,
                                     UserRepository userRepository,
                                     TokenService tokenService,
                                     SessionIssuer sessionIssuer) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.sessionIssuer = sessionIssuer;
    }

    /**
     * Rotates a session: the presented refresh token is consumed and a brand new
     * pair of tokens is issued. Because a token can be exchanged only once, replay
     * of a stolen token fails and leaves the victim with a dead session instead of
     * silently handing the attacker access.
     */
    @Override
    public Optional<AuthenticatedUser> handle(RefreshSessionCommand command) {
        var claims = tokenService.parseRefreshToken(command.refreshToken());
        if (claims.isEmpty()) {
            LOGGER.warn("Session refresh rejected: token is not a valid refresh token");
            return Optional.empty();
        }

        var storedToken = refreshTokenRepository.findByTokenHash(sessionIssuer.hashToken(command.refreshToken()));
        if (storedToken.isEmpty()) {
            LOGGER.warn("Session refresh rejected: unknown refresh token");
            return Optional.empty();
        }

        if (!storedToken.get().isValid()) {
            LOGGER.warn("Session refresh rejected: token for user {} was already used, revoked or expired",
                    storedToken.get().getUserId());
            return Optional.empty();
        }

        var user = userRepository.findByEmail(claims.get().username());
        if (user.isEmpty()) {
            LOGGER.warn("Session refresh rejected: subject {} no longer exists", claims.get().username());
            return Optional.empty();
        }

        if (!storedToken.get().getUserId().equals(user.get().getId().value())) {
            LOGGER.warn("Session refresh rejected: token subject does not match its owner");
            return Optional.empty();
        }

        storedToken.get().markAsUsed();
        refreshTokenRepository.save(storedToken.get());
        LOGGER.info("Session refreshed for user {}", claims.get().username());

        return Optional.of(sessionIssuer.issue(user.get()));
    }

    /**
     * Revokes exactly the presented token. Unknown or malformed tokens are
     * rejected so sign out cannot be used to probe the token store.
     */
    @Override
    public boolean handle(RevokeSessionCommand command) {
        var claims = tokenService.parseRefreshToken(command.refreshToken());
        if (claims.isEmpty()) {
            LOGGER.warn("Session revoke rejected: token is not a valid refresh token");
            return false;
        }

        var storedToken = refreshTokenRepository.findByTokenHash(sessionIssuer.hashToken(command.refreshToken()));
        if (storedToken.isEmpty()) {
            LOGGER.warn("Session revoke rejected: unknown refresh token");
            return false;
        }

        if (storedToken.get().getRevokedAt() == null) {
            storedToken.get().revoke();
            refreshTokenRepository.save(storedToken.get());
            LOGGER.info("Session revoked for user {}", claims.get().username());
        }
        return true;
    }
}
