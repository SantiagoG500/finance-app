package com.finance.transaction.infraestructure.driver_adapters.feign;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.transaction.domain.model.CurrencyMetadata;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestCountriesFeignAdapterTest {

    private static final String TEST_API_KEY = "test-restcountries-key";
    private static final String BEARER_HEADER = "Bearer " + TEST_API_KEY;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RestCountriesFeignClient restCountriesFeignClient;

    private RestCountriesFeignAdapter restCountriesFeignAdapter;

    @BeforeEach
    void setUp() {
        restCountriesFeignAdapter = new RestCountriesFeignAdapter(restCountriesFeignClient, TEST_API_KEY);
    }

    @Test
    void shouldReturnCurrencyMetadataForCopUsdEurMxnAndV5FormatsWithAndWithoutCurrenciesNode() throws Exception {
        // Arrange
        JsonNode copResponse = objectMapper.readTree("""
                [
                  {
                    "name": { "common": "Colombia", "official": "Republic of Colombia" },
                    "currencies": {
                      "COP": { "name": "Colombian peso", "symbol": "$" }
                    }
                  }
                ]
                """);
        JsonNode usdResponseWithoutCurrencies = objectMapper.readTree("""
                {
                  "data": [
                    {
                      "name": "United States"
                    }
                  ]
                }
                """);
        JsonNode eurResponse = objectMapper.readTree("""
                {
                  "results": {
                    "name": { "official": "Federal Republic of Germany" },
                    "currencies": {
                      "OTHER": { "name": "Euro", "symbol": "€" }
                    }
                  }
                }
                """);
        JsonNode mxnResponseWithSingularCurrency = objectMapper.readTree("""
                {
                  "countries": [
                    {
                      "country_name": "Mexico",
                      "currency": { "name": "Mexican peso", "symbol": "$" }
                    }
                  ]
                }
                """);
        JsonNode gbpResponseWithCommonNameAndArrayCurrencies = objectMapper.readTree("""
                {
                  "items": [
                    {
                      "common_name": "United Kingdom",
                      "currencies": [
                        { "name": "British pound", "symbol": "£" }
                      ]
                    }
                  ]
                }
                """);

        when(restCountriesFeignClient.searchCountries(BEARER_HEADER, "colombia")).thenReturn(copResponse);
        when(restCountriesFeignClient.searchCountries(BEARER_HEADER, "united states")).thenReturn(usdResponseWithoutCurrencies);
        when(restCountriesFeignClient.searchCountries(BEARER_HEADER, "germany")).thenReturn(eurResponse);
        when(restCountriesFeignClient.searchCountries(BEARER_HEADER, "mexico")).thenReturn(mxnResponseWithSingularCurrency);
        when(restCountriesFeignClient.searchCountries(BEARER_HEADER, "united kingdom")).thenReturn(gbpResponseWithCommonNameAndArrayCurrencies);

        // Act
        CurrencyMetadata copMeta = restCountriesFeignAdapter.getCurrencyMetadata(" cop ");
        CurrencyMetadata usdMeta = restCountriesFeignAdapter.getCurrencyMetadata("USD");
        CurrencyMetadata eurMeta = restCountriesFeignAdapter.getCurrencyMetadata("EUR");
        CurrencyMetadata mxnMeta = restCountriesFeignAdapter.getCurrencyMetadata("MXN");
        CurrencyMetadata gbpMeta = restCountriesFeignAdapter.getCurrencyMetadata("GBP");

        // Assert
        assertNotNull(copMeta);
        assertEquals("COP", copMeta.code());
        assertEquals("Colombian peso", copMeta.name());
        assertEquals("$", copMeta.symbol());
        assertEquals(List.of("Colombia"), copMeta.countries());

        assertEquals("USD", usdMeta.code());
        assertEquals("United States dollar", usdMeta.name());
        assertEquals("$", usdMeta.symbol());
        assertEquals(List.of("United States"), usdMeta.countries());

        assertEquals("EUR", eurMeta.code());
        assertEquals("Euro", eurMeta.name());
        assertEquals("€", eurMeta.symbol());
        assertEquals(List.of("Federal Republic of Germany"), eurMeta.countries());

        assertEquals("MXN", mxnMeta.code());
        assertEquals("Mexican peso", mxnMeta.name());
        assertEquals(List.of("Mexico"), mxnMeta.countries());

        assertEquals("GBP", gbpMeta.code());
        assertEquals("British pound", gbpMeta.name());
        assertEquals("£", gbpMeta.symbol());
        assertEquals(List.of("United Kingdom"), gbpMeta.countries());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenApiKeyIsNullOrBlank() {
        // Arrange
        RestCountriesFeignAdapter nullKeyAdapter = new RestCountriesFeignAdapter(restCountriesFeignClient);
        RestCountriesFeignAdapter blankKeyAdapter = new RestCountriesFeignAdapter(restCountriesFeignClient, " ");

        // Act
        IllegalStateException exNull = assertThrows(
                IllegalStateException.class,
                () -> nullKeyAdapter.getCurrencyMetadata("COP")
        );
        IllegalStateException exBlank = assertThrows(
                IllegalStateException.class,
                () -> blankKeyAdapter.getCurrencyMetadata("COP")
        );

        // Assert
        assertEquals("La llave RESTCOUNTRIES_API_KEY no está configurada", exNull.getMessage());
        assertEquals("La llave RESTCOUNTRIES_API_KEY no está configurada", exBlank.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenCurrencyIsUnsupportedOrResponseIsNullOrEmpty() throws Exception {
        // Arrange
        JsonNode emptyArray = objectMapper.readTree("[]");
        JsonNode emptyObjectInArray = objectMapper.readTree("[{}]");
        when(restCountriesFeignClient.searchCountries(BEARER_HEADER, "colombia"))
                .thenReturn(null)
                .thenReturn(emptyArray)
                .thenReturn(emptyObjectInArray);

        // Act
        IllegalArgumentException exUnknownCode = assertThrows(
                IllegalArgumentException.class,
                () -> restCountriesFeignAdapter.getCurrencyMetadata("XXX")
        );
        IllegalArgumentException exNull = assertThrows(
                IllegalArgumentException.class,
                () -> restCountriesFeignAdapter.getCurrencyMetadata("COP")
        );
        IllegalArgumentException exEmpty = assertThrows(
                IllegalArgumentException.class,
                () -> restCountriesFeignAdapter.getCurrencyMetadata("COP")
        );
        IllegalArgumentException exUnmatched = assertThrows(
                IllegalArgumentException.class,
                () -> restCountriesFeignAdapter.getCurrencyMetadata("COP")
        );

        // Assert
        assertEquals("Moneda no soportada por el proveedor externo: XXX", exUnknownCode.getMessage());
        assertEquals("Moneda no soportada por el proveedor externo: COP", exNull.getMessage());
        assertEquals("Moneda no soportada por el proveedor externo: COP", exEmpty.getMessage());
        assertEquals("Moneda no soportada por el proveedor externo: COP", exUnmatched.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFeignNotFoundOccurs() {
        // Arrange
        Request request = Request.create(Request.HttpMethod.GET, "/countries/v5?q=colombia", Collections.emptyMap(), null, new RequestTemplate());
        FeignException.NotFound notFound = new FeignException.NotFound("Not Found", request, null, Collections.emptyMap());
        when(restCountriesFeignClient.searchCountries(BEARER_HEADER, "colombia")).thenThrow(notFound);

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> restCountriesFeignAdapter.getCurrencyMetadata("COP")
        );

        // Assert
        assertEquals("Moneda no soportada por el proveedor externo: COP", exception.getMessage());
    }

    @Test
    void shouldRethrowFeignExceptionWhenGenericFeignExceptionOccurs() {
        // Arrange
        Request request = Request.create(Request.HttpMethod.GET, "/countries/v5?q=colombia", Collections.emptyMap(), null, new RequestTemplate());
        FeignException.ServiceUnavailable unavailable = new FeignException.ServiceUnavailable(
                "Down",
                request,
                "down".getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );
        when(restCountriesFeignClient.searchCountries(BEARER_HEADER, "colombia")).thenThrow(unavailable);

        // Act
        FeignException exception = assertThrows(
                FeignException.class,
                () -> restCountriesFeignAdapter.getCurrencyMetadata("COP")
        );

        // Assert
        assertSame(unavailable, exception);
    }
}
