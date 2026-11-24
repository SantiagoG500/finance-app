package com.finance.transaction.infraestructure.driver_adapters.feign;

import com.fasterxml.jackson.databind.JsonNode;
import com.finance.transaction.domain.model.gateway.ExchangeRateGateway;
import com.finance.transaction.infraestructure.driver_adapters.feign.dto.ExchangeRateResponseDTO;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Component
public class ExchangeRateFeignAdapter implements ExchangeRateGateway {

    private final ExchangeRateFeignClient exchangeRateFeignClient;

    @Value("${services.exchangerate.api-key:}")
    private String apiKey;

    @Autowired
    public ExchangeRateFeignAdapter(ExchangeRateFeignClient exchangeRateFeignClient) {
        this.exchangeRateFeignClient = exchangeRateFeignClient;
    }

    public ExchangeRateFeignAdapter(ExchangeRateFeignClient exchangeRateFeignClient, String apiKey) {
        this.exchangeRateFeignClient = exchangeRateFeignClient;
        this.apiKey = apiKey;
    }

    @Override
    @Cacheable(value = "exchangeRates", key = "#p0 + '-' + #p1")
    public BigDecimal getExchangeRate(String baseCurrency, String targetCurrency) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("La llave EXCHANGERATE_API_KEY no está configurada");
        }
        String normalizedBase = baseCurrency == null ? "" : baseCurrency.trim().toUpperCase(Locale.ROOT);
        String normalizedTarget = targetCurrency == null ? "" : targetCurrency.trim().toUpperCase(Locale.ROOT);

        try {
            ExchangeRateResponseDTO response = exchangeRateFeignClient.getLatestRates(apiKey.trim(), normalizedBase);
            if (response == null
                    || !"success".equalsIgnoreCase(response.getResult())
                    || response.getConversionRates() == null
                    || !response.getConversionRates().containsKey(normalizedTarget)
                    || response.getConversionRates().get(normalizedTarget) == null) {
                throw new IllegalArgumentException("Par de divisas no soportado: " + normalizedBase + " -> " + normalizedTarget);
            }
            return response.getConversionRates().get(normalizedTarget);
        } catch (FeignException.NotFound e) {
            log.warn("Moneda no encontrada en ExchangeRate-API ({} -> {}): {}", normalizedBase, normalizedTarget, e.getMessage());
            throw new IllegalArgumentException("Moneda origen no soportada: " + normalizedBase);
        } catch (FeignException e) {
            log.warn("Error al consultar ExchangeRate-API ({} -> {}): {}", normalizedBase, normalizedTarget, e.getMessage());
            throw e;
        }
    }

    @Override
    @Cacheable(value = "currencyCodes", key = "#p0")
    public Optional<String> getOfficialCurrencyName(String currencyCode) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("La llave EXCHANGERATE_API_KEY no está configurada");
        }
        String normalizedCode = currencyCode == null ? "" : currencyCode.trim().toUpperCase(Locale.ROOT);

        try {
            JsonNode root = exchangeRateFeignClient.getSupportedCodes(apiKey.trim());
            if (root != null
                    && "success".equalsIgnoreCase(root.path("result").asText())
                    && root.path("supported_codes").isArray()) {
                for (JsonNode item : root.path("supported_codes")) {
                    if (item != null
                            && item.isArray()
                            && item.size() >= 2
                            && item.get(0).asText("").equalsIgnoreCase(normalizedCode)) {
                        return Optional.of(item.get(1).asText(""));
                    }
                }
            }
            return Optional.empty();
        } catch (FeignException e) {
            log.warn("Error al consultar catálogo /codes en ExchangeRate-API ({}): {}", normalizedCode, e.getMessage());
            throw e;
        }
    }
}
