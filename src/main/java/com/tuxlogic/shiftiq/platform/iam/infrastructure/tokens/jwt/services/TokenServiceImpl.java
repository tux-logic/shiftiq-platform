package com.tuxlogic.shiftiq.platform.iam.infrastructure.tokens.jwt.services;

import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.tokens.RefreshTokenClaims;
import com.tuxlogic.shiftiq.platform.iam.infrastructure.tokens.jwt.BearerTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
public class TokenServiceImpl implements BearerTokenService {

    /**
     * Audience of short lived access tokens: the only kind accepted by the request filter.
     */
    private static final String ACCESS_TOKEN_AUDIENCE = "shiftiq-users";

    /**
     * Audience of long lived refresh tokens: usable only on the refresh endpoint.
     */
    private static final String REFRESH_TOKEN_AUDIENCE = "shiftiq-refresh";

    private final SecretKey signingKey;
    private final int accessTokenExpirationMinutes;
    private final int refreshTokenExpirationDays;

    public TokenServiceImpl(
            @Value("${authorization.jwt.secret}") String secret,
            @Value("${authorization.jwt.access-token.expiration.minutes:15}") int accessTokenExpirationMinutes,
            @Value("${authorization.jwt.refresh-token.expiration.days:7}") int refreshTokenExpirationDays) {
        this.signingKey = buildSigningKey(secret);
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
    }

    /**
     * Builds the signing key eagerly so an unusable secret fails the application at
     * startup instead of failing on the first sign-in. HS256 requires at least 256 bits.
     */
    private static SecretKey buildSigningKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "authorization.jwt.secret is not set. Provide a Base64 encoded secret of at least 32 bytes.");
        }
        final byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "authorization.jwt.secret must be a valid Base64 value", ex);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "authorization.jwt.secret must decode to at least 32 bytes (256 bits) to sign tokens with HS256, got "
                            + keyBytes.length);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateToken(Authentication authentication) {
        return generateToken(authentication.getName());
    }

    @Override
    public String generateToken(String username) {
        long expiresInMillis = accessTokenExpirationMinutes * 60L * 1000L;
        return buildToken(ACCESS_TOKEN_AUDIENCE, new HashMap<>(), username, expiresInMillis);
    }

    @Override
    public String generateRefreshToken(String username) {
        long expiresInMillis = refreshTokenExpirationDays * 24L * 60L * 60L * 1000L;
        return buildToken(REFRESH_TOKEN_AUDIENCE, new HashMap<>(), username, expiresInMillis);
    }

    @Override
    public Optional<RefreshTokenClaims> parseRefreshToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            var claims = extractAllClaims(token);
            if (claims.getAudience() == null || !claims.getAudience().contains(REFRESH_TOKEN_AUDIENCE)) {
                return Optional.empty();
            }
            return Optional.of(new RefreshTokenClaims(claims.getSubject(), claims.getExpiration().toInstant()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String buildToken(String audience, Map<String, Object> extraClaims, String username, long expiresInMillis) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuer("shiftiq-platform")
                .audience().add(audience).and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiresInMillis))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * A token is usable as an access token only when its signature and expiry are
     * valid and it was issued as an access token: refresh tokens must never be
     * accepted by the request filter.
     */
    @Override
    public boolean validateToken(String token) {
        try {
            var claims = extractAllClaims(token);
            return claims.getAudience() != null && claims.getAudience().contains(ACCESS_TOKEN_AUDIENCE);
        } catch (Exception e) {
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public String getBearerTokenFrom(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
