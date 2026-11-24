package com.finance.account.infraestructure.adapters.security;

import com.finance.account.domain.gateway.TokenBlacklistGateway;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryTokenBlacklistAdapter implements TokenBlacklistGateway {

    private record ActiveSessionEntry(String token, Instant expiresAt) {
    }

    private final Map<String, Instant> revokedTokens = new ConcurrentHashMap<>();
    private final Map<String, ActiveSessionEntry> activeSessions = new ConcurrentHashMap<>();

    @Override
    public void revokeToken(String token, Instant expiresAt) {
        if (token == null || token.isBlank()) {
            return;
        }
        purgeExpiredEntries();
        String trimmedToken = token.trim();
        Instant effectiveExpiration = expiresAt != null ? expiresAt : Instant.now().plusSeconds(86400);
        if (effectiveExpiration.isAfter(Instant.now())) {
            revokedTokens.put(trimmedToken, effectiveExpiration);
        }
        clearActiveSessionByToken(trimmedToken);
    }

    @Override
    public boolean isTokenRevoked(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        purgeExpiredEntries();
        String trimmedToken = token.trim();
        Instant expiresAt = revokedTokens.get(trimmedToken);
        if (expiresAt == null) {
            return false;
        }
        if (!expiresAt.isAfter(Instant.now())) {
            revokedTokens.remove(trimmedToken);
            return false;
        }
        return true;
    }

    @Override
    public boolean hasActiveSession(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        purgeExpiredEntries();
        String normalizedEmail = email.trim().toLowerCase();
        ActiveSessionEntry entry = activeSessions.get(normalizedEmail);
        return entry != null && entry.expiresAt().isAfter(Instant.now());
    }

    @Override
    public void registerActiveSession(String email, String token, Instant expiresAt) {
        if (email == null || email.isBlank() || token == null || token.isBlank()) {
            return;
        }
        purgeExpiredEntries();
        String normalizedEmail = email.trim().toLowerCase();
        String trimmedToken = token.trim();
        ActiveSessionEntry previous = activeSessions.get(normalizedEmail);
        if (previous != null && !previous.token().equals(trimmedToken) && previous.expiresAt().isAfter(Instant.now())) {
            revokedTokens.put(previous.token(), previous.expiresAt());
        }
        Instant effectiveExpiration = expiresAt != null ? expiresAt : Instant.now().plusSeconds(3600);
        if (effectiveExpiration.isAfter(Instant.now())) {
            activeSessions.put(normalizedEmail, new ActiveSessionEntry(trimmedToken, effectiveExpiration));
        }
    }

    @Override
    public void clearActiveSessionByToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        String trimmedToken = token.trim();
        activeSessions.entrySet().removeIf(entry -> trimmedToken.equals(entry.getValue().token()));
    }

    @Override
    public void revokeActiveSessionByEmail(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        purgeExpiredEntries();
        String normalizedEmail = email.trim().toLowerCase();
        ActiveSessionEntry previous = activeSessions.remove(normalizedEmail);
        if (previous != null && previous.expiresAt().isAfter(Instant.now())) {
            revokedTokens.put(previous.token(), previous.expiresAt());
        }
    }

    private void purgeExpiredEntries() {
        Instant now = Instant.now();
        revokedTokens.entrySet().removeIf(entry -> !entry.getValue().isAfter(now));
        activeSessions.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
    }
}
