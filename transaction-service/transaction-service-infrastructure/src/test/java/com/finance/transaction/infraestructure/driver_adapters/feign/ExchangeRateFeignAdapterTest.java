package com.finance.transaction.infraestructure.driver_adapters.feign;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.transaction.infraestructure.driver_adapters.feign.dto.ExchangeRateResponseDTO;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateFeignAdapterTest {

    private static final String TEST_API_KEY = "test-exchangerate-key";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ExchangeRateFeignClient exchangeRateFeignClient;

    private ExchangeRateFeignAdapter exchangeRateFeignAdapter;

    @BeforeEach
    void setUp() {
        exchangeRateFeignAdapter = new ExchangeRateFeignAdapter(exchangeRateFeignClient, TEST_API_KEY);
    }

    @Test
    void shouldReturnRealExchangeRatesForCopToUsdAndEurToCop() {
        // Arrange
        ExchangeRateResponseDTO copResponse = new ExchangeRateResponseDTO(
                "success",
                "COP",
                Map.of("USD", new BigDecimal("0.00025"), "EUR", new BigDecimal("0.00023"))
        );
        ExchangeRateResponseDTO eurResponse = new ExchangeRateResponseDTO(
                "success",
                "EUR",
                Map.of("COP", new BigDecimal("4350.50"), "USD", new BigDecimal("1.08"))
        );
        when(exchangeRateFeignClient.getLatestRates(TEST_API_KEY, "COP")).thenReturn(copResponse);
        when(exchangeRateFeignClient.getLatestRates(TEST_API_KEY, "EUR")).thenReturn(eurResponse);

        // Act
        BigDecimal copToUsd = exchangeRateFeignAdapter.getExchangeRate(" cop ", " usd ");
        BigDecimal eurToCop = exchangeRateFeignAdapter.getExchangeRate("EUR", "COP");

        // Assert
        assertEquals(new BigDecimal("0.00025"), copToUsd);
        assertEquals(new BigDecimal("4350.50"), eurToCop);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenApiKeyIsNullOrBlank() {
        // Arrange
        ExchangeRateFeignAdapter nullKeyAdapter = new ExchangeRateFeignAdapter(exchangeRateFeignClient);
        ExchangeRateFeignAdapter blankKeyAdapter = new ExchangeRateFeignAdapter(exchangeRateFeignClient, "   ");

        // Act
        IllegalStateException exNull = assertThrows(
                IllegalStateException.class,
                () -> nullKeyAdapter.getExchangeRate("COP", "USD")
        );
        IllegalStateException exBlank = assertThrows(
                IllegalStateException.class,
                () -> blankKeyAdapter.getExchangeRate("COP", "USD")
        );
        IllegalStateException exCodesNull = assertThrows(
                IllegalStateException.class,
                () -> nullKeyAdapter.getOfficialCurrencyName("COP")
        );

        // Assert
        assertEquals("La llave EXCHANGERATE_API_KEY no está configurada", exNull.getMessage());
        assertEquals("La llave EXCHANGERATE_API_KEY no está configurada", exBlank.getMessage());
        assertEquals("La llave EXCHANGERATE_API_KEY no está configurada", exCodesNull.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenResponseIsInvalidOrTargetCurrencyIsMissing() {
        // Arrange
        ExchangeRateResponseDTO errorResult = new ExchangeRateResponseDTO("error", "USD", Map.of("COP", BigDecimal.ONE));
        ExchangeRateResponseDTO nullRates = new ExchangeRateResponseDTO("success", "USD", null);
        ExchangeRateResponseDTO emptyRates = new ExchangeRateResponseDTO("success", "USD", Collections.emptyMap());
        when(exchangeRateFeignClient.getLatestRates(TEST_API_KEY, "USD"))
                .thenReturn(null)
                .thenReturn(errorResult)
                .thenReturn(nullRates)
                .thenReturn(emptyRates);

        // Act
        IllegalArgumentException exNull = assertThrows(
                IllegalArgumentException.class,
                () -> exchangeRateFeignAdapter.getExchangeRate("USD", "XYZ")
        );
        IllegalArgumentException exErrorResult = assertThrows(
                IllegalArgumentException.class,
                () -> exchangeRateFeignAdapter.getExchangeRate("USD", "COP")
        );
        IllegalArgumentException exNullRates = assertThrows(
                IllegalArgumentException.class,
                () -> exchangeRateFeignAdapter.getExchangeRate("USD", "COP")
        );
        IllegalArgumentException exMissingTarget = assertThrows(
                IllegalArgumentException.class,
                () -> exchangeRateFeignAdapter.getExchangeRate("USD", "XYZ")
        );

        // Assert
        assertEquals("Par de divisas no soportado: USD -> XYZ", exNull.getMessage());
        assertEquals("Par de divisas no soportado: USD -> COP", exErrorResult.getMessage());
        assertEquals("Par de divisas no soportado: USD -> COP", exNullRates.getMessage());
        assertEquals("Par de divisas no soportado: USD -> XYZ", exMissingTarget.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFeignNotFoundOccurs() {
        // Arrange
        Request request = Request.create(Request.HttpMethod.GET, "/latest/INVALID", Collections.emptyMap(), null, new RequestTemplate());
        FeignException.NotFound notFound = new FeignException.NotFound("Not Found", request, null, Collections.emptyMap());
        when(exchangeRateFeignClient.getLatestRates(TEST_API_KEY, "INVALID")).thenThrow(notFound);

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> exchangeRateFeignAdapter.getExchangeRate("INVALID", "USD")
        );

        // Assert
        assertEquals("Moneda origen no soportada: INVALID", exception.getMessage());
    }

    @Test
    void shouldRethrowFeignExceptionWhenExternalProviderFailsWith5xxOrTimeout() {
        // Arrange
        Request request = Request.create(Request.HttpMethod.GET, "/latest/COP", Collections.emptyMap(), null, new RequestTemplate());
        FeignException.InternalServerError serverError = new FeignException.InternalServerError(
                "Error",
                request,
                "error".getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );
        when(exchangeRateFeignClient.getLatestRates(TEST_API_KEY, "COP")).thenThrow(serverError);

        // Act
        FeignException exception = assertThrows(
                FeignException.class,
                () -> exchangeRateFeignAdapter.getExchangeRate("COP", "USD")
        );

        // Assert
        assertEquals(500, exception.status());
    }

    @Test
    void shouldReturnOfficialCurrencyNameFromSupportedCodesForCopAndUsdAndEmptyWhenMissing() throws Exception {
        // Arrange
        JsonNode codesResponse = objectMapper.readTree("""
                {
                  "result": "success",
                  "supported_codes": [
                    ["COP", "Colombian Peso"],
                    ["USD", "United States Dollar"],
                    ["EUR", "Euro"]
                  ]
                }
                """);
        JsonNode errorResponse = objectMapper.readTree("""
                {
                  "result": "error"
                }
                """);
        when(exchangeRateFeignClient.getSupportedCodes(TEST_API_KEY))
                .thenReturn(codesResponse)
                .thenReturn(codesResponse)
                .thenReturn(codesResponse)
                .thenReturn(errorResponse)
                .thenReturn(null);

        // Act
        Optional<String> copName = exchangeRateFeignAdapter.getOfficialCurrencyName(" cop ");
        Optional<String> usdName = exchangeRateFeignAdapter.getOfficialCurrencyName("USD");
        Optional<String> missingCode = exchangeRateFeignAdapter.getOfficialCurrencyName("XXX");
        Optional<String> errorResult = exchangeRateFeignAdapter.getOfficialCurrencyName("COP");
        Optional<String> nullResult = exchangeRateFeignAdapter.getOfficialCurrencyName("COP");

        // Assert
        assertTrue(copName.isPresent());
        assertEquals("Colombian Peso", copName.get());
        assertTrue(usdName.isPresent());
        assertEquals("United States Dollar", usdName.get());
        assertFalse(missingCode.isPresent());
        assertFalse(errorResult.isPresent());
        assertFalse(nullResult.isPresent());
    }

    @Test
    void shouldRethrowFeignExceptionWhenGetSupportedCodesFailsWith5xx() {
        // Arrange
        Request request = Request.create(Request.HttpMethod.GET, "/codes", Collections.emptyMap(), null, new RequestTemplate());
        FeignException.ServiceUnavailable unavailable = new FeignException.ServiceUnavailable(
                "Unavailable",
                request,
                "down".getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );
        when(exchangeRateFeignClient.getSupportedCodes(TEST_API_KEY)).thenThrow(unavailable);

        // Act
        FeignException exception = assertThrows(
                FeignException.class,
                () -> exchangeRateFeignAdapter.getOfficialCurrencyName("COP")
        );

        // Assert
        assertEquals(503, exception.status());
    }
}
