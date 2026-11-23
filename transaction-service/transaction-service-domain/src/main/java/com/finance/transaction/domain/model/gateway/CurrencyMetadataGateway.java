package com.finance.transaction.domain.model.gateway;

import com.finance.transaction.domain.model.CurrencyMetadata;

public interface CurrencyMetadataGateway {

    CurrencyMetadata getCurrencyMetadata(String currencyCode);

}
