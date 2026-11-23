package com.finance.transaction.domain.usecase;

import com.finance.transaction.domain.model.ConvertedBalance;
import com.finance.transaction.domain.model.CurrencyMetadata;
import com.finance.transaction.domain.model.gateway.CurrencyMetadataGateway;
import com.finance.transaction.domain.model.gateway.ExchangeRateGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DivisasUseCaseTest {

    @Mock
    private ProyeccionesUseCase proyeccionesUseCase;

    @Mock
    private ExchangeRateGateway exchangeRateGateway;

    @Mock
    private CurrencyMetadataGateway currencyMetadataGateway;

    @InjectMocks
    private DivisasUseCase divisasUseCase;

    @Test
    void shouldConvertMonthlyBalanceWhenCurrenciesAreDifferent() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        when(proyeccionesUseCase.calcularBalanceNeto(userId)).thenReturn(new BigDecimal("1000.00"));
        when(exchangeRateGateway.getExchangeRate("USD", "EUR")).thenReturn(new BigDecimal("0.92"));

        // Act
        ConvertedBalance result = divisasUseCase.convertirBalanceMensual(userId, " usd ", " eur ");

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.usuarioId());
        assertEquals("USD", result.baseCurrency());
        assertEquals("EUR", result.targetCurrency());
        assertEquals(new BigDecimal("0.92"), result.exchangeRate());
        assertEquals(new BigDecimal("1000.00"), result.originalBalance());
        assertEquals(new BigDecimal("920.00"), result.convertedBalance());
    }

    @Test
    void shouldUseExchangeRateOneWhenBaseAndTargetCurrencyAreEqual() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        when(proyeccionesUseCase.calcularBalanceNeto(userId)).thenReturn(new BigDecimal("500.00"));

        // Act
        ConvertedBalance result = divisasUseCase.convertirBalanceMensual(userId, "usd", "USD");

        // Assert
        assertEquals(BigDecimal.ONE, result.exchangeRate());
        assertEquals(new BigDecimal("500.00"), result.convertedBalance());
        verifyNoInteractions(exchangeRateGateway);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenConvertParamsOrExchangeRateAreInvalid() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        when(proyeccionesUseCase.calcularBalanceNeto(userId)).thenReturn(new BigDecimal("100.00"));
        when(exchangeRateGateway.getExchangeRate("USD", "COP")).thenReturn(BigDecimal.ZERO);

        // Act
        IllegalArgumentException exUser = assertThrows(
                IllegalArgumentException.class,
                () -> divisasUseCase.convertirBalanceMensual(" ", "USD", "EUR")
        );
        IllegalArgumentException exBaseNull = assertThrows(
                IllegalArgumentException.class,
                () -> divisasUseCase.convertirBalanceMensual(userId, null, "EUR")
        );
        IllegalArgumentException exTargetFormat = assertThrows(
                IllegalArgumentException.class,
                () -> divisasUseCase.convertirBalanceMensual(userId, "USD", "EURO")
        );
        IllegalArgumentException exRateZero = assertThrows(
                IllegalArgumentException.class,
                () -> divisasUseCase.convertirBalanceMensual(userId, "USD", "COP")
        );

        // Assert
        assertEquals("El ID externo del usuario no puede ser nulo o vacío.", exUser.getMessage());
        assertEquals("La moneda origen debe ser un código ISO de 3 letras", exBaseNull.getMessage());
        assertEquals("La moneda destino debe ser un código ISO de 3 letras", exTargetFormat.getMessage());
        assertEquals("Tasa de cambio no disponible para la moneda solicitada", exRateZero.getMessage());
    }

    @Test
    void shouldGetCurrencyMetadataSuccessfullyWhenCodeIsValid() {
        // Arrange
        CurrencyMetadata expected = new CurrencyMetadata("COP", "Colombian peso", "$", List.of("Colombia"));
        when(currencyMetadataGateway.getCurrencyMetadata("COP")).thenReturn(expected);

        // Act
        CurrencyMetadata actual = divisasUseCase.consultarMetadatosMoneda(" cop ");

        // Assert
        assertNotNull(actual);
        assertEquals("COP", actual.code());
        assertEquals("Colombian peso", actual.name());
        assertEquals("$", actual.symbol());
        assertEquals(List.of("Colombia"), actual.countries());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenCurrencyCodeIsInvalidForMetadata() {
        // Arrange
        String invalidCode = "12";

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> divisasUseCase.consultarMetadatosMoneda(invalidCode)
        );

        // Assert
        assertEquals("El código de moneda debe ser un código ISO de 3 letras", exception.getMessage());
    }
}
