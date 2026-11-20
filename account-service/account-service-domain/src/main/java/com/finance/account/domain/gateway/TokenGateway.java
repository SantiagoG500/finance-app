package com.finance.account.domain.gateway;

import java.util.UUID;

public interface TokenGateway {

    String generateToken(UUID userId, String email, String fullName);

    String extractEmail(String token);

    UUID extractUserId(String token);

    boolean validateToken(String token);

}
