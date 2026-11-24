package com.finance.account.infraestructure.adapters.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryTokenBlacklistAdapterTest {

    private InMemoryTokenBlacklistAdapter blacklistAdapter;

    @BeforeEach
    void setUp() {
        blacklistAdapter = new InMemoryTokenBlacklistAdapter();
    }

    @Test
    void shouldRevokeTokenAndReportAsRevokedWhenNotExpired() {
        // Arrange
        String token = "jwt.token.active";
        String defaultExpiryToken = "jwt.token.default.expiry";
        Instant expiresAt = Instant.now().plusSeconds(3600);

        // Act
        blacklistAdapter.revokeToken(token, expiresAt);
        blacklistAdapter.revokeToken(defaultExpiryToken, null);
        boolean revoked = blacklistAdapter.isTokenRevoked(token);
        boolean defaultExpiryRevoked = blacklistAdapter.isTokenRevoked(defaultExpiryToken);

        // Assert
        assertTrue(revoked);
        assertTrue(defaultExpiryRevoked);
    }

    @Test
    void shouldReturnFalseWhenTokenWasNotRevokedOrExpiredOrBlank() throws InterruptedException {
        // Arrange
        String shortLivedToken = "jwt.token.short";
        String expiredToken = "jwt.token.expired";
        Instant pastExpiration = Instant.now().minusSeconds(60);

        // Act
        blacklistAdapter.revokeToken(shortLivedToken, Instant.now().plusMillis(30));
        Thread.sleep(60);
        blacklistAdapter.revokeToken(expiredToken, pastExpiration);
        blacklistAdapter.revokeToken(null, Instant.now().plusSeconds(3600));
        blacklistAdapter.revokeToken("   ", Instant.now().plusSeconds(3600));
        boolean isShortLivedRevoked = blacklistAdapter.isTokenRevoked(shortLivedToken);
        boolean isExpiredRevoked = blacklistAdapter.isTokenRevoked(expiredToken);
        boolean isUnknownRevoked = blacklistAdapter.isTokenRevoked("unknown.token");
        boolean isNullRevoked = blacklistAdapter.isTokenRevoked(null);
        boolean isBlankRevoked = blacklistAdapter.isTokenRevoked("  ");

        // Assert
        assertFalse(isShortLivedRevoked);
        assertFalse(isExpiredRevoked);
        assertFalse(isUnknownRevoked);
        assertFalse(isNullRevoked);
        assertFalse(isBlankRevoked);
    }

    @Test
    void shouldTrackActiveSessionClearOnLogoutAndAllowNewSessionRegistration() {
        // Arrange
        String email = "  USER@finance.com ";
        String firstToken = "jwt.token.session.1";
        String secondToken = "jwt.token.session.2";
        Instant expiresAt = Instant.now().plusSeconds(3600);

        // Act
        blacklistAdapter.registerActiveSession(email, firstToken, expiresAt);
        boolean activeAfterFirstLogin = blacklistAdapter.hasActiveSession("user@finance.com");
        blacklistAdapter.revokeToken(firstToken, expiresAt);
        boolean activeAfterLogout = blacklistAdapter.hasActiveSession("user@finance.com");
        boolean firstTokenRevoked = blacklistAdapter.isTokenRevoked(firstToken);
        blacklistAdapter.registerActiveSession("user@finance.com", secondToken, null);
        boolean activeAfterSecondLogin = blacklistAdapter.hasActiveSession("user@finance.com");
        blacklistAdapter.clearActiveSessionByToken(secondToken);
        boolean activeAfterClear = blacklistAdapter.hasActiveSession("user@finance.com");

        // Assert
        assertTrue(activeAfterFirstLogin);
        assertFalse(activeAfterLogout);
        assertTrue(firstTokenRevoked);
        assertTrue(activeAfterSecondLogin);
        assertFalse(activeAfterClear);
    }

    @Test
    void shouldHandleInvalidOrExpiredActiveSessionsGracefully() throws InterruptedException {
        // Arrange
        String shortSessionEmail = "short@finance.com";
        Instant pastExpiration = Instant.now().minusSeconds(60);

        // Act
        blacklistAdapter.registerActiveSession(null, "token", Instant.now().plusSeconds(60));
        blacklistAdapter.registerActiveSession(" ", "token", Instant.now().plusSeconds(60));
        blacklistAdapter.registerActiveSession("a@b.com", null, Instant.now().plusSeconds(60));
        blacklistAdapter.registerActiveSession("a@b.com", "  ", Instant.now().plusSeconds(60));
        blacklistAdapter.registerActiveSession("past@finance.com", "token-past", pastExpiration);
        blacklistAdapter.registerActiveSession(shortSessionEmail, "token-short", Instant.now().plusMillis(30));
        Thread.sleep(60);
        blacklistAdapter.clearActiveSessionByToken(null);
        blacklistAdapter.clearActiveSessionByToken("   ");
        boolean hasNullEmailSession = blacklistAdapter.hasActiveSession(null);
        boolean hasBlankEmailSession = blacklistAdapter.hasActiveSession("   ");
        boolean hasPastSession = blacklistAdapter.hasActiveSession("past@finance.com");
        boolean hasShortSession = blacklistAdapter.hasActiveSession(shortSessionEmail);

        // Assert
        assertFalse(hasNullEmailSession);
        assertFalse(hasBlankEmailSession);
        assertFalse(hasPastSession);
        assertFalse(hasShortSession);
    }
}
