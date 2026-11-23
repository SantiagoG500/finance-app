package com.finance.transaction.infraestructure.driver_adapters.feign;

import com.finance.transaction.infraestructure.driver_adapters.feign.dto.FrankfurterResponseDTO;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FrankfurterFeignAdapterTest {

    @Mock
    private FrankfurterFeignClient frankfurterFeignClient;

    @InjectMocks
    private FrankfurterFeignAdapter frankfurterFeignAdapter;

    @Test
    void shouldReturnExchangeRateWhenResponseContainsTargetCurrency() {
        // Arrange
        FrankfurterResponseDTO dto = new FrankfurterResponseDTO(
                BigDecimal.ONE,
                "USD",
                "2026-11-23",
                Map.of("EUR", new BigDecimal("0.92"))
        );
        when(frankfurterFeignClient.getLatestRate("USD", "EUR")).thenReturn(dto);

        // Act
        BigDecimal rate = frankfurterFeignAdapter.getExchangeRate("USD", "EUR");

        // Assert
        assertEquals(new BigDecimal("0.92"), rate);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenResponseOrRatesMapIsMissingTargetCurrency() {
        // Arrange
        FrankfurterResponseDTO emptyRates = new FrankfurterResponseDTO(BigDecimal.ONE, "USD", "2026-11-23", Collections.emptyMap());
        when(frankfurterFeignClient.getLatestRate("USD", "XYZ"))
                .thenReturn(null)
                .thenReturn(emptyRates);

        // Act
        IllegalArgumentException exNull = assertThrows(
                IllegalArgumentException.class,
                () -> frankfurterFeignAdapter.getExchangeRate("USD", "XYZ")
        );
        IllegalArgumentException exEmpty = assertThrows(
                IllegalArgumentException.class,
                () -> frankfurterFeignAdapter.getExchangeRate("USD", "XYZ")
        );

        // Assert
        assertEquals("Moneda no soportada por el proveedor externo", exNull.getMessage());
        assertEquals("Moneda no soportada por el proveedor externo", exEmpty.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFeignNotFoundOccurs() {
        // Arrange
        Request request = Request.create(Request.HttpMethod.GET, "/latest", Collections.emptyMap(), null, new RequestTemplate());
        FeignException.NotFound notFound = new FeignException.NotFound("Not Found", request, null, Collections.emptyMap());
        when(frankfurterFeignClient.getLatestRate("USD", "INVALID")).thenThrow(notFound);

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> frankfurterFeignAdapter.getExchangeRate("USD", "INVALID")
        );

        // Assert
        assertEquals("Moneda no soportada por el proveedor externo", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenGenericFeignExceptionOccurs() {
        // Arrange
        Request request = Request.create(Request.HttpMethod.GET, "/latest", Collections.emptyMap(), null, new RequestTemplate());
        FeignException.InternalServerError serverError = new FeignException.InternalServerError(
                "Error",
                request,
                "error".getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );
        when(frankfurterFeignClient.getLatestRate("USD", "EUR")).thenThrow(serverError);

        // Act
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> frankfurterFeignAdapter.getExchangeRate("USD", "EUR")
        );

        // Assert
        assertEquals("Proveedor externo de divisas temporalmente no disponible", exception.getMessage());
    }
}
