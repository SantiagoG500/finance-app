package com.finance.transaction.domain.model;

public record VerifiedUserSession(
        String id,
        String fullName,
        String token
) {
}
