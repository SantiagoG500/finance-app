package com.finance.transaction.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record ConvertedBalance(
        String usuarioId,
        String targetCurrency,
        String targetCurrencyName,
        String targetCurrencySymbol,
        List<String> targetCurrencyCountries,
        BigDecimal convertedBalance,
        int transactionsProcessed
) {
}
