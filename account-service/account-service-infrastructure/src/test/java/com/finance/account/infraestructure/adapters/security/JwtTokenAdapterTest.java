package com.finance.account.infraestructure.adapters.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenAdapterTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private JwtTokenAdapter jwtTokenAdapter;

    @BeforeEach
    void setUp() {
        jwtTokenAdapter = new JwtTokenAdapter(SECRET, 3600000L);
    }

    @Test
    void shouldGenerateAndValidateTokenAndExtractClaimsSuccessfully() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String email = "jwt@example.com";
        String fullName = "Jwt User";

        // Act
        String token = jwtTokenAdapter.generateToken(userId, email, fullName);
        boolean isValid = jwtTokenAdapter.validateToken(token);
        UUID extractedUserId = jwtTokenAdapter.extractUserId(token);
        String extractedEmail = jwtTokenAdapter.extractEmail(token);

        // Assert
        assertNotNull(token);
        assertTrue(isValid);
        assertEquals(userId, extractedUserId);
        assertEquals(email, extractedEmail);
    }

    @Test
    void shouldReturnFalseWhenValidatingTamperedOrMalformedToken() {
        // Arrange
        String malformedToken = "invalid.jwt.token";

        // Act
        boolean isValid = jwtTokenAdapter.validateToken(malformedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void shouldReturnFalseWhenTokenIsSignedWithDifferentSecret() {
        // Arrange
        JwtTokenAdapter otherAdapter = new JwtTokenAdapter(
                "9999999999999999999999999999999999999999999999999999999999999999",
                3600000L
        );
        String tokenFromOtherSecret = otherAdapter.generateToken(UUID.randomUUID(), "a@b.com", "Name");

        // Act
        boolean isValid = jwtTokenAdapter.validateToken(tokenFromOtherSecret);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void shouldReturnFalseWhenValidatingExpiredToken() {
        // Arrange
        JwtTokenAdapter expiredAdapter = new JwtTokenAdapter(SECRET, -10000L);
        String expiredToken = expiredAdapter.generateToken(UUID.randomUUID(), "exp@example.com", "Expired User");

        // Act
        boolean isValid = jwtTokenAdapter.validateToken(expiredToken);

        // Assert
        assertFalse(isValid);
    }
}
