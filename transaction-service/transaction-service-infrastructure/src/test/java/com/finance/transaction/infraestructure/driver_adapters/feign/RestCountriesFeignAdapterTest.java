package com.finance.transaction.infraestructure.driver_adapters.feign;

import com.finance.transaction.domain.model.CurrencyMetadata;
import com.finance.transaction.infraestructure.driver_adapters.feign.dto.RestCountryResponseDTO;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestCountriesFeignAdapterTest {

    @Mock
    private RestCountriesFeignClient restCountriesFeignClient;

    @InjectMocks
    private RestCountriesFeignAdapter restCountriesFeignAdapter;

    @Test
    void shouldReturnCurrencyMetadataWhenCountriesAreFound() {
        // Arrange
        RestCountryResponseDTO country = new RestCountryResponseDTO(
                new RestCountryResponseDTO.CountryNameDTO("Colombia", "Republic of Colombia"),
                Map.of("COP", new RestCountryResponseDTO.CurrencyInfoDTO("Colombian peso", "$"))
        );
        when(restCountriesFeignClient.getCountriesByCurrency("COP", "name,currencies"))
                .thenReturn(List.of(country));

        // Act
        CurrencyMetadata metadata = restCountriesFeignAdapter.getCurrencyMetadata("COP");

        // Assert
        assertNotNull(metadata);
        assertEquals("COP", metadata.code());
        assertEquals("Colombian peso", metadata.name());
        assertEquals("$", metadata.symbol());
        assertEquals(List.of("Colombia"), metadata.countries());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenCountryListIsNullOrEmpty() {
        // Arrange
        when(restCountriesFeignClient.getCountriesByCurrency("XYZ", "name,currencies"))
                .thenReturn(null)
                .thenReturn(Collections.emptyList());

        // Act
        IllegalArgumentException exNull = assertThrows(
                IllegalArgumentException.class,
                () -> restCountriesFeignAdapter.getCurrencyMetadata("XYZ")
        );
        IllegalArgumentException exEmpty = assertThrows(
                IllegalArgumentException.class,
                () -> restCountriesFeignAdapter.getCurrencyMetadata("XYZ")
        );

        // Assert
        assertEquals("Moneda no soportada por el proveedor externo", exNull.getMessage());
        assertEquals("Moneda no soportada por el proveedor externo", exEmpty.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFeignNotFoundOccurs() {
        // Arrange
        Request request = Request.create(Request.HttpMethod.GET, "/currency/INVALID", Collections.emptyMap(), null, new RequestTemplate());
        FeignException.NotFound notFound = new FeignException.NotFound("Not Found", request, null, Collections.emptyMap());
        when(restCountriesFeignClient.getCountriesByCurrency("INVALID", "name,currencies")).thenThrow(notFound);

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> restCountriesFeignAdapter.getCurrencyMetadata("INVALID")
        );

        // Assert
        assertEquals("Moneda no soportada por el proveedor externo", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenGenericFeignExceptionOccurs() {
        // Arrange
        Request request = Request.create(Request.HttpMethod.GET, "/currency/COP", Collections.emptyMap(), null, new RequestTemplate());
        FeignException.ServiceUnavailable unavailable = new FeignException.ServiceUnavailable(
                "Down",
                request,
                "down".getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );
        when(restCountriesFeignClient.getCountriesByCurrency("COP", "name,currencies")).thenThrow(unavailable);

        // Act
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> restCountriesFeignAdapter.getCurrencyMetadata("COP")
        );

        // Assert
        assertEquals("Proveedor externo de divisas temporalmente no disponible", exception.getMessage());
    }
}
