package com.finance.account.domain.gateway;

import java.time.Instant;

public interface TokenBlacklistGateway {

    void revokeToken(String token, Instant expiresAt);

    boolean isTokenRevoked(String token);

    boolean hasActiveSession(String email);

    void registerActiveSession(String email, String token, Instant expiresAt);

    void clearActiveSessionByToken(String token);

    void revokeActiveSessionByEmail(String email);
}
