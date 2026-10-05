package com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.tokens;

import java.util.Optional;

public interface TokenService {
    String generateToken(String username);
    String generateRefreshToken(String username);
    Optional<RefreshTokenClaims> parseRefreshToken(String token);
    String getUsernameFromToken(String token);
    boolean validateToken(String token);
}
