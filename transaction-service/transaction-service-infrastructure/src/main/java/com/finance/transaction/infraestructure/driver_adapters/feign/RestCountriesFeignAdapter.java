package com.finance.transaction.infraestructure.driver_adapters.feign;

import com.fasterxml.jackson.databind.JsonNode;
import com.finance.transaction.domain.model.CurrencyMetadata;
import com.finance.transaction.domain.model.gateway.CurrencyMetadataGateway;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
public class RestCountriesFeignAdapter implements CurrencyMetadataGateway {

    private record IsoCurrencyCatalogEntry(String searchTerm, String defaultName, String defaultSymbol) {
    }

    private static final Map<String, IsoCurrencyCatalogEntry> ISO_CURRENCY_CATALOG = Map.ofEntries(
            Map.entry("COP", new IsoCurrencyCatalogEntry("colombia", "Colombian peso", "$")),
            Map.entry("USD", new IsoCurrencyCatalogEntry("united states", "United States dollar", "$")),
            Map.entry("EUR", new IsoCurrencyCatalogEntry("germany", "Euro", "€")),
            Map.entry("MXN", new IsoCurrencyCatalogEntry("mexico", "Mexican peso", "$")),
            Map.entry("GBP", new IsoCurrencyCatalogEntry("united kingdom", "British pound", "£")),
            Map.entry("JPY", new IsoCurrencyCatalogEntry("japan", "Japanese yen", "¥")),
            Map.entry("BRL", new IsoCurrencyCatalogEntry("brazil", "Brazilian real", "R$")),
            Map.entry("ARS", new IsoCurrencyCatalogEntry("argentina", "Argentine peso", "$")),
            Map.entry("CLP", new IsoCurrencyCatalogEntry("chile", "Chilean peso", "$")),
            Map.entry("PEN", new IsoCurrencyCatalogEntry("peru", "Peruvian sol", "S/")),
            Map.entry("CAD", new IsoCurrencyCatalogEntry("canada", "Canadian dollar", "$")),
            Map.entry("CHF", new IsoCurrencyCatalogEntry("switzerland", "Swiss franc", "Fr"))
    );

    private final RestCountriesFeignClient restCountriesFeignClient;

    @Value("${services.restcountries.api-key:}")
    private String apiKey;

    @Autowired
    public RestCountriesFeignAdapter(RestCountriesFeignClient restCountriesFeignClient) {
        this.restCountriesFeignClient = restCountriesFeignClient;
    }

    public RestCountriesFeignAdapter(RestCountriesFeignClient restCountriesFeignClient, String apiKey) {
        this.restCountriesFeignClient = restCountriesFeignClient;
        this.apiKey = apiKey;
    }

    @Override
    @Cacheable(value = "currencyMetadata", key = "#p0")
    public CurrencyMetadata getCurrencyMetadata(String currencyCode) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("La llave RESTCOUNTRIES_API_KEY no está configurada");
        }
        String normalizedCode = currencyCode == null ? "" : currencyCode.trim().toUpperCase(Locale.ROOT);
        IsoCurrencyCatalogEntry catalogEntry = ISO_CURRENCY_CATALOG.get(normalizedCode);
        if (catalogEntry == null) {
            throw new IllegalArgumentException("Moneda no soportada por el proveedor externo: " + normalizedCode);
        }

        try {
            JsonNode responseNode = restCountriesFeignClient.searchCountries(
                    "Bearer " + apiKey.trim(),
                    catalogEntry.searchTerm()
            );
            List<JsonNode> countryNodes = extractCountryNodes(responseNode);

            if (countryNodes.isEmpty()) {
                throw new IllegalArgumentException("Moneda no soportada por el proveedor externo: " + normalizedCode);
            }

            String currencyName = catalogEntry.defaultName();
            String currencySymbol = catalogEntry.defaultSymbol();
            List<String> countries = new ArrayList<>();

            for (JsonNode country : countryNodes) {
                String countryName = extractCountryName(country);
                if (!countryName.isBlank()) {
                    countries.add(countryName);
                }

                JsonNode currencyInfo = resolveCurrencyInfo(country, normalizedCode);
                if (currencyInfo != null && !currencyInfo.isMissingNode() && !currencyInfo.isNull()) {
                    String candidateName = currencyInfo.path("name").asText("").trim();
                    if (!candidateName.isBlank()) {
                        currencyName = candidateName;
                    }
                    String candidateSymbol = currencyInfo.path("symbol").asText("").trim();
                    if (!candidateSymbol.isBlank()) {
                        currencySymbol = candidateSymbol;
                    }
                }
            }

            if (countries.isEmpty()) {
                throw new IllegalArgumentException("Moneda no soportada por el proveedor externo: " + normalizedCode);
            }

            return new CurrencyMetadata(normalizedCode, currencyName, currencySymbol, countries);
        } catch (FeignException.NotFound e) {
            log.warn("Moneda no encontrada en REST Countries API ({}): {}", normalizedCode, e.getMessage());
            throw new IllegalArgumentException("Moneda no soportada por el proveedor externo: " + normalizedCode);
        } catch (FeignException e) {
            log.warn("Error al consultar REST Countries API ({}): {}", normalizedCode, e.getMessage());
            throw e;
        }
    }

    private List<JsonNode> extractCountryNodes(JsonNode root) {
        List<JsonNode> nodes = new ArrayList<>();
        if (root == null || root.isNull() || root.isMissingNode()) {
            return nodes;
        }
        if (root.isArray()) {
            root.forEach(nodes::add);
            return nodes;
        }
        JsonNode container = resolveWrapperNode(root);
        if (container != null && !container.isNull()) {
            if (container.isArray()) {
                container.forEach(nodes::add);
            } else if (container.isObject() && !container.isEmpty()) {
                nodes.add(container);
            }
        }
        return nodes;
    }

    private JsonNode resolveWrapperNode(JsonNode root) {
        for (String wrapperField : List.of("data", "results", "countries", "items")) {
            if (root.has(wrapperField)) {
                return root.get(wrapperField);
            }
        }
        return root;
    }

    private String extractCountryName(JsonNode country) {
        JsonNode nameNode = country.path("name");
        if (nameNode.isTextual()) {
            return nameNode.asText("").trim();
        }
        String common = nameNode.path("common").asText("").trim();
        if (!common.isBlank()) {
            return common;
        }
        String official = nameNode.path("official").asText("").trim();
        if (!official.isBlank()) {
            return official;
        }
        String countryName = country.path("country_name").asText("").trim();
        if (!countryName.isBlank()) {
            return countryName;
        }
        return country.path("common_name").asText("").trim();
    }

    private JsonNode resolveCurrencyInfo(JsonNode countryNode, String normalizedCode) {
        JsonNode currenciesNode = countryNode.path("currencies");
        if (!currenciesNode.isMissingNode() && !currenciesNode.isNull()) {
            if (currenciesNode.isObject()) {
                if (currenciesNode.has(normalizedCode)) {
                    return currenciesNode.get(normalizedCode);
                }
                Iterator<JsonNode> elements = currenciesNode.elements();
                if (elements.hasNext()) {
                    return elements.next();
                }
            } else if (currenciesNode.isArray() && !currenciesNode.isEmpty()) {
                return currenciesNode.get(0);
            }
        }
        JsonNode singleCurrencyNode = countryNode.path("currency");
        if (singleCurrencyNode.isObject() && !singleCurrencyNode.isEmpty()) {
            return singleCurrencyNode;
        }
        return null;
    }
}
