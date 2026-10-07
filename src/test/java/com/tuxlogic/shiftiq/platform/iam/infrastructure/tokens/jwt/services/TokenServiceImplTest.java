package com.tuxlogic.shiftiq.platform.iam.infrastructure.tokens.jwt.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TokenServiceImplTest {

    private TokenServiceImpl tokenService;

    @BeforeEach
    void setUp() {
        // 256-bit (32 bytes) base64 secret
        String secret = Base64.getEncoder().encodeToString("12345678901234567890123456789012".getBytes());
        tokenService = new TokenServiceImpl(secret, 15, 7);
    }

    @Test
    @DisplayName("generateToken and validateToken succeed for access token")
    void generateAndValidateAccessToken() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@shiftiq.com");

        String token = tokenService.generateToken(auth);

        assertThat(token).isNotBlank();
        assertThat(tokenService.validateToken(token)).isTrue();
        assertThat(tokenService.getUsernameFromToken(token)).isEqualTo("test@shiftiq.com");
    }

    @Test
    @DisplayName("generateRefreshToken and parseRefreshToken succeed for refresh token")
    void generateAndParseRefreshToken() {
        String refreshToken = tokenService.generateRefreshToken("test@shiftiq.com");

        assertThat(refreshToken).isNotBlank();
        // Refresh token must NOT be valid as an access token
        assertThat(tokenService.validateToken(refreshToken)).isFalse();

        Optional<?> claims = tokenService.parseRefreshToken(refreshToken);
        assertThat(claims).isPresent();
    }

    @Test
    @DisplayName("parseRefreshToken rejects access token as refresh token")
    void parseRefreshTokenRejectsAccessToken() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@shiftiq.com");

        String accessToken = tokenService.generateToken(auth);

        assertThat(tokenService.parseRefreshToken(accessToken)).isEmpty();
    }
}
