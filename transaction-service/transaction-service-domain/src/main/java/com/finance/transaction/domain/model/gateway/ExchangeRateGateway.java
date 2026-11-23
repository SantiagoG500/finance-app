package com.finance.transaction.domain.model.gateway;

import java.math.BigDecimal;

public interface ExchangeRateGateway {

    BigDecimal getExchangeRate(String baseCurrency, String targetCurrency);

}
