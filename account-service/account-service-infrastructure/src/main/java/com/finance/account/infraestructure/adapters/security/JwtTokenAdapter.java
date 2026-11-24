package com.finance.account.infraestructure.adapters.security;

import com.finance.account.domain.gateway.TokenGateway;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@NoArgsConstructor
@AllArgsConstructor
public class JwtTokenAdapter implements TokenGateway {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        return new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Override
    public String generateToken(UUID userId, String email, String fullName) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(email)
                .claim("userId", userId.toString())
                .claim("fullName", fullName)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    @Override
    public UUID extractUserId(String token) {
        String userIdStr = getClaims(token).get("userId", String.class);
        return UUID.fromString(userIdStr);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public Instant extractExpiration(String token) {
        return getClaims(token).getExpiration().toInstant();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
