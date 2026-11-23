package com.finance.transaction.infraestructure.driver_adapters.feign;

import com.finance.transaction.domain.model.CurrencyMetadata;
import com.finance.transaction.domain.model.gateway.CurrencyMetadataGateway;
import com.finance.transaction.infraestructure.driver_adapters.feign.dto.RestCountryResponseDTO;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestCountriesFeignAdapter implements CurrencyMetadataGateway {

    private static final String DEFAULT_FIELDS = "name,currencies";

    private final RestCountriesFeignClient restCountriesFeignClient;

    @Override
    @Cacheable(value = "currencyMetadata", key = "#currencyCode")
    public CurrencyMetadata getCurrencyMetadata(String currencyCode) {
        try {
            List<RestCountryResponseDTO> countriesResponse = restCountriesFeignClient.getCountriesByCurrency(
                    currencyCode,
                    DEFAULT_FIELDS
            );

            if (countriesResponse == null || countriesResponse.isEmpty()) {
                throw new IllegalArgumentException("Moneda no soportada por el proveedor externo");
            }

            String currencyName = currencyCode;
            String currencySymbol = "";
            List<String> countries = new ArrayList<>();

            for (RestCountryResponseDTO country : countriesResponse) {
                if (country == null) {
                    continue;
                }
                if (country.getName() != null
                        && country.getName().getCommon() != null
                        && !country.getName().getCommon().isBlank()) {
                    countries.add(country.getName().getCommon());
                }

                Map<String, RestCountryResponseDTO.CurrencyInfoDTO> currencies = country.getCurrencies();
                if (currencies != null && !currencies.isEmpty()) {
                    RestCountryResponseDTO.CurrencyInfoDTO info = currencies.get(currencyCode);
                    if (info == null) {
                        info = currencies.values().iterator().next();
                    }
                    if (info != null) {
                        if (info.getName() != null && !info.getName().isBlank()) {
                            currencyName = info.getName();
                        }
                        if (info.getSymbol() != null && !info.getSymbol().isBlank()) {
                            currencySymbol = info.getSymbol();
                        }
                    }
                }
            }

            return new CurrencyMetadata(currencyCode, currencyName, currencySymbol, countries);
        } catch (FeignException.NotFound e) {
            log.warn("Moneda no encontrada en REST Countries API ({}): {}", currencyCode, e.getMessage());
            throw new IllegalArgumentException("Moneda no soportada por el proveedor externo");
        } catch (FeignException e) {
            log.warn("Error al consultar REST Countries API ({}): {}", currencyCode, e.getMessage());
            throw new IllegalStateException("Proveedor externo de divisas temporalmente no disponible");
        }
    }
}
