package com.finance.transaction.domain.model;

public record VerifiedUserSession(
        String id,
        String fullName,
        String token,
        String baseCurrency
) {
    public VerifiedUserSession(String id, String fullName, String token) {
        this(id, fullName, token, "USD");
    }

    public String resolvedBaseCurrency() {
        return (baseCurrency == null || baseCurrency.isBlank())
                ? "USD"
                : baseCurrency.trim().toUpperCase();
    }
}
