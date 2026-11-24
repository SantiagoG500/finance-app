package com.finance.transaction.domain.model;

import java.util.List;

public record CurrencyMetadata(
        String code,
        String name,
        String symbol,
        List<String> countries
) {
}
