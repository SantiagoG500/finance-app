package com.finance.transaction.infraestructure.driver_adapters.feign;

import com.finance.transaction.domain.model.gateway.ExchangeRateGateway;
import com.finance.transaction.infraestructure.driver_adapters.feign.dto.FrankfurterResponseDTO;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class FrankfurterFeignAdapter implements ExchangeRateGateway {

    private final FrankfurterFeignClient frankfurterFeignClient;

    @Override
    @Cacheable(value = "exchangeRates", key = "#baseCurrency + '-' + #targetCurrency")
    public BigDecimal getExchangeRate(String baseCurrency, String targetCurrency) {
        try {
            FrankfurterResponseDTO response = frankfurterFeignClient.getLatestRate(baseCurrency, targetCurrency);
            if (response == null
                    || response.getRates() == null
                    || !response.getRates().containsKey(targetCurrency)
                    || response.getRates().get(targetCurrency) == null) {
                throw new IllegalArgumentException("Moneda no soportada por el proveedor externo");
            }
            return response.getRates().get(targetCurrency);
        } catch (FeignException.NotFound e) {
            log.warn("Moneda no encontrada en Frankfurter API ({} -> {}): {}", baseCurrency, targetCurrency, e.getMessage());
            throw new IllegalArgumentException("Moneda no soportada por el proveedor externo");
        } catch (FeignException e) {
            log.warn("Error al consultar Frankfurter API ({} -> {}): {}", baseCurrency, targetCurrency, e.getMessage());
            throw new IllegalStateException("Proveedor externo de divisas temporalmente no disponible");
        }
    }
}
