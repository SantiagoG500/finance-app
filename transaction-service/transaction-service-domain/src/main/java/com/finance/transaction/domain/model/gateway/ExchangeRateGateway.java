package com.finance.transaction.domain.model.gateway;

import java.math.BigDecimal;
import java.util.Optional;

public interface ExchangeRateGateway {

    BigDecimal getExchangeRate(String baseCurrency, String targetCurrency);

    Optional<String> getOfficialCurrencyName(String currencyCode);

}
