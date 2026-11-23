package com.finance.transaction.domain.model;

import java.math.BigDecimal;

public record ConvertedBalance(
        String usuarioId,
        String baseCurrency,
        String targetCurrency,
        BigDecimal exchangeRate,
        BigDecimal originalBalance,
        BigDecimal convertedBalance
) {
}
