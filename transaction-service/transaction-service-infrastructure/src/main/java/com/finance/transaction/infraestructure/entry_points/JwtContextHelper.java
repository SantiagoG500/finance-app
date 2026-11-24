package com.finance.transaction.infraestructure.entry_points;

import org.springframework.security.oauth2.jwt.Jwt;

public final class JwtContextHelper {

    private JwtContextHelper() {
    }

    public static String extractUserId(Jwt jwt) {
        if (jwt != null) {
            String userIdClaim = jwt.getClaimAsString("userId");
            if (userIdClaim != null && !userIdClaim.trim().isEmpty()) {
                return userIdClaim.trim();
            }
            if (jwt.getSubject() != null && !jwt.getSubject().trim().isEmpty()) {
                return jwt.getSubject().trim();
            }
        }
        throw new IllegalArgumentException("El token JWT no contiene un identificador de usuario válido");
    }

    public static String extractUserEmail(Jwt jwt) {
        String emailClaim = jwt.getClaimAsString("email");
        return emailClaim != null ? emailClaim : jwt.getSubject();
    }

    public static String resolveBearerToken(String authorizationHeader, Jwt jwt) {
        return authorizationHeader != null ? authorizationHeader : "Bearer " + jwt.getTokenValue();
    }
}
