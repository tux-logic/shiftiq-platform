package com.tuxlogic.shiftiq.platform.iam.infrastructure.tokens.jwt.services;

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
import java.util.function.Function;

@Service
public class TokenServiceImpl implements BearerTokenService {

    private final String secret;
    private final int expirationDays;

    public TokenServiceImpl(
            @Value("${authorization.jwt.secret}") String secret,
            @Value("${authorization.jwt.expiration.days:30}") int expirationDays) {
        this.secret = secret;
        this.expirationDays = expirationDays;
    }

    @Override
    public String generateToken(Authentication authentication) {
        return generateToken(authentication.getName());
    }

    @Override
    public String generateToken(String username) {
        return buildToken(new HashMap<>(), username);
    }

    private String buildToken(Map<String, Object> extraClaims, String username) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuer("shiftiq-platform")
                .audience().add("shiftiq-users").and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationDays * 24L * 60L * 60L * 1000L))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
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
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
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
