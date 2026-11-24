package com.finance.transaction.domain.usecase;

import com.finance.transaction.domain.model.ConvertedBalance;
import com.finance.transaction.domain.model.CurrencyMetadata;
import com.finance.transaction.domain.model.Transaccion;
import com.finance.transaction.domain.model.VerifiedUserSession;
import com.finance.transaction.domain.model.enumns.FrecuenciaType;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import com.finance.transaction.domain.model.gateway.AccountVerificationGateway;
import com.finance.transaction.domain.model.gateway.CurrencyMetadataGateway;
import com.finance.transaction.domain.model.gateway.ExchangeRateGateway;
import com.finance.transaction.domain.model.gateway.TransaccionGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DivisasUseCaseTest {

    @Mock
    private TransaccionGateway transaccionGateway;

    @Mock
    private ExchangeRateGateway exchangeRateGateway;

    @Mock
    private CurrencyMetadataGateway currencyMetadataGateway;

    @Mock
    private AccountVerificationGateway accountVerificationGateway;

    private DivisasUseCase divisasUseCase;

    @BeforeEach
    void setUp() {
        divisasUseCase = new DivisasUseCase(
                transaccionGateway,
                exchangeRateGateway,
                currencyMetadataGateway,
                accountVerificationGateway
        );
        lenient().when(accountVerificationGateway.verifyLoggedUser(anyString(), any()))
                .thenAnswer(inv -> new VerifiedUserSession(inv.getArgument(0), "Test User", "jwt-token", "USD"));
    }

    @Test
    void shouldConvert1500000CopToUsdVerifyLoggedUserAndEnrichWithRestCountriesMetadata() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        Transaccion copIncome = buildTransaction(userId, new BigDecimal("1500000.00"), "COP", TransaccionType.INCOME, FrecuenciaType.MONTHLY);
        CurrencyMetadata usdMetadata = new CurrencyMetadata("USD", "United States dollar", "$", List.of("United States"));

        when(currencyMetadataGateway.getCurrencyMetadata("USD")).thenReturn(usdMetadata);
        when(transaccionGateway.obtenerTransaccionesPorUsuario(userId)).thenReturn(List.of(copIncome));
        when(exchangeRateGateway.getExchangeRate("COP", "USD")).thenReturn(new BigDecimal("0.00025"));

        // Act
        ConvertedBalance result = divisasUseCase.convertirBalanceMensual(userId, " usd ", "Bearer jwt-token");

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.usuarioId());
        assertEquals("USD", result.targetCurrency());
        assertEquals("United States dollar", result.targetCurrencyName());
        assertEquals("$", result.targetCurrencySymbol());
        assertEquals(List.of("United States"), result.targetCurrencyCountries());
        assertEquals(new BigDecimal("375.00"), result.convertedBalance());
        assertEquals(1, result.transactionsProcessed());
        verify(accountVerificationGateway).verifyLoggedUser(userId, "Bearer jwt-token");
        verify(transaccionGateway, times(1)).obtenerTransaccionesPorUsuario(userId);
        verify(exchangeRateGateway, times(1)).getExchangeRate("COP", "USD");
    }

    @Test
    void shouldFallbackToDefaultCurrencyMetadataWhenRestCountriesThrowsRuntimeExceptionDuringBalanceConversion() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        Transaccion copIncome = buildTransaction(userId, new BigDecimal("1000000.00"), "COP", TransaccionType.INCOME, FrecuenciaType.MONTHLY);

        when(currencyMetadataGateway.getCurrencyMetadata("USD")).thenThrow(new RuntimeException("REST Countries timeout"));
        when(transaccionGateway.obtenerTransaccionesPorUsuario(userId)).thenReturn(List.of(copIncome));
        when(exchangeRateGateway.getExchangeRate("COP", "USD")).thenReturn(new BigDecimal("0.00025"));

        // Act
        ConvertedBalance result = divisasUseCase.convertirBalanceMensual(userId, "USD", "Bearer jwt-token");

        // Assert
        assertNotNull(result);
        assertEquals("USD", result.targetCurrency());
        assertEquals("USD", result.targetCurrencyName());
        assertEquals("$", result.targetCurrencySymbol());
        assertTrue(result.targetCurrencyCountries().isEmpty());
        assertEquals(new BigDecimal("250.00"), result.convertedBalance());
        assertEquals(1, result.transactionsProcessed());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenAccountVerificationReturnsNullOrBlankSessionId() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        when(accountVerificationGateway.verifyLoggedUser(userId, "Bearer bad"))
                .thenReturn(null)
                .thenReturn(new VerifiedUserSession(" ", "User", "bad"));

        // Act
        IllegalStateException exNull = assertThrows(
                IllegalStateException.class,
                () -> divisasUseCase.convertirBalanceMensual(userId, "USD", "Bearer bad")
        );
        IllegalStateException exBlank = assertThrows(
                IllegalStateException.class,
                () -> divisasUseCase.convertirBalanceMensual(userId, "USD", "Bearer bad")
        );

        // Assert
        assertEquals("La sesión del usuario no pudo ser verificada", exNull.getMessage());
        assertEquals("La sesión del usuario no pudo ser verificada", exBlank.getMessage());
    }

    @Test
    void shouldConvertEurToCopAndConsolidateMultipleFrequenciesAndCurrenciesDeduplicatingExchangeRateCalls() {
        // Arrange
        DivisasUseCase useCaseThreeArgs = new DivisasUseCase(transaccionGateway, exchangeRateGateway, currencyMetadataGateway);
        String userId = UUID.randomUUID().toString();
        Transaccion eurIncome1 = buildTransaction(userId, new BigDecimal("600.00"), "EUR", TransaccionType.INCOME, FrecuenciaType.MONTHLY);
        Transaccion eurIncome2 = buildTransaction(userId, new BigDecimal("400.00"), "EUR", TransaccionType.INCOME, FrecuenciaType.MONTHLY);
        Transaccion copExpenseDaily = buildTransaction(userId, new BigDecimal("10000.00"), "COP", TransaccionType.EXPENSE, FrecuenciaType.DAILY);
        Transaccion copIncomeWeekly = buildTransaction(userId, new BigDecimal("50000.00"), "COP", TransaccionType.INCOME, FrecuenciaType.WEEKLY);
        Transaccion copExpenseYearly = buildTransaction(userId, new BigDecimal("1200000.00"), "COP", TransaccionType.EXPENSE, FrecuenciaType.YEARLY);
        Transaccion copOneTime = buildTransaction(userId, new BigDecimal("50000.00"), "COP", TransaccionType.INCOME, null);
        Transaccion blankCurrencyUsdTx = buildTransaction(userId, new BigDecimal("10.00"), " ", TransaccionType.INCOME, FrecuenciaType.ONE_TIME);
        CurrencyMetadata copMetadata = new CurrencyMetadata("COP", "Colombian peso", "$", List.of("Colombia"));

        when(currencyMetadataGateway.getCurrencyMetadata("COP")).thenReturn(copMetadata);
        when(transaccionGateway.obtenerTransaccionesPorUsuario(userId)).thenReturn(Arrays.asList(
                eurIncome1,
                eurIncome2,
                copExpenseDaily,
                copIncomeWeekly,
                copExpenseYearly,
                copOneTime,
                blankCurrencyUsdTx,
                null
        ));
        when(exchangeRateGateway.getExchangeRate("EUR", "COP")).thenReturn(new BigDecimal("4350.00"));
        when(exchangeRateGateway.getExchangeRate("USD", "COP")).thenReturn(new BigDecimal("4000.00"));

        // Act
        ConvertedBalance result = useCaseThreeArgs.convertirBalanceMensual(userId, "cop");

        // Assert
        assertEquals("COP", result.targetCurrency());
        assertEquals("Colombian peso", result.targetCurrencyName());
        assertEquals("$", result.targetCurrencySymbol());
        assertEquals(List.of("Colombia"), result.targetCurrencyCountries());
        assertEquals(new BigDecimal("4240000.00"), result.convertedBalance());
        assertEquals(7, result.transactionsProcessed());
        verify(exchangeRateGateway, times(1)).getExchangeRate("EUR", "COP");
        verify(exchangeRateGateway, times(1)).getExchangeRate("USD", "COP");
    }

    @Test
    void shouldHandleNullTransactionsAndFallbackMetadataFieldsWhenConvertingBalance() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        when(currencyMetadataGateway.getCurrencyMetadata("MXN")).thenReturn(null);
        when(transaccionGateway.obtenerTransaccionesPorUsuario(userId)).thenReturn(null);

        // Act
        ConvertedBalance result = divisasUseCase.convertirBalanceMensual(userId, "MXN");

        // Assert
        assertEquals("MXN", result.targetCurrency());
        assertEquals("MXN", result.targetCurrencyName());
        assertEquals("$", result.targetCurrencySymbol());
        assertTrue(result.targetCurrencyCountries().isEmpty());
        assertEquals(new BigDecimal("0.00"), result.convertedBalance());
        assertEquals(0, result.transactionsProcessed());
        verifyNoInteractions(exchangeRateGateway);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenConvertParamsOrExchangeRateAreInvalid() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        Transaccion usdTx = buildTransaction(userId, new BigDecimal("100.00"), "USD", TransaccionType.INCOME, FrecuenciaType.MONTHLY);
        when(currencyMetadataGateway.getCurrencyMetadata("COP"))
                .thenReturn(new CurrencyMetadata("COP", "Colombian peso", "$", List.of("Colombia")));
        when(transaccionGateway.obtenerTransaccionesPorUsuario(userId)).thenReturn(List.of(usdTx));
        when(exchangeRateGateway.getExchangeRate("USD", "COP")).thenReturn(BigDecimal.ZERO);

        // Act
        IllegalArgumentException exUser = assertThrows(
                IllegalArgumentException.class,
                () -> divisasUseCase.convertirBalanceMensual(" ", "EUR")
        );
        IllegalArgumentException exTargetNull = assertThrows(
                IllegalArgumentException.class,
                () -> divisasUseCase.convertirBalanceMensual(userId, null)
        );
        IllegalArgumentException exTargetFormat = assertThrows(
                IllegalArgumentException.class,
                () -> divisasUseCase.convertirBalanceMensual(userId, "EURO")
        );
        IllegalArgumentException exRateZero = assertThrows(
                IllegalArgumentException.class,
                () -> divisasUseCase.convertirBalanceMensual(userId, "COP")
        );

        // Assert
        assertEquals("El ID externo del usuario no puede ser nulo o vacío.", exUser.getMessage());
        assertEquals("La moneda destino debe ser un código ISO de 3 letras", exTargetNull.getMessage());
        assertEquals("La moneda destino debe ser un código ISO de 3 letras", exTargetFormat.getMessage());
        assertEquals("Tasa de cambio no disponible para la moneda solicitada", exRateZero.getMessage());
    }

    @Test
    void shouldGetCurrencyMetadataDirectlyFromExchangeRateGatewayWithoutCallingRestCountries() {
        // Arrange
        when(exchangeRateGateway.getOfficialCurrencyName("COP")).thenReturn(Optional.of("Colombian Peso"));
        when(exchangeRateGateway.getOfficialCurrencyName("USD")).thenReturn(Optional.of("United States Dollar"));
        when(exchangeRateGateway.getOfficialCurrencyName("EUR")).thenReturn(Optional.of("Euro"));
        when(exchangeRateGateway.getOfficialCurrencyName("MXN")).thenReturn(Optional.of("Mexican Peso"));
        when(exchangeRateGateway.getOfficialCurrencyName("GBP")).thenReturn(Optional.of("Pound Sterling"));
        when(exchangeRateGateway.getOfficialCurrencyName("JPY")).thenReturn(Optional.of("Japanese Yen"));
        when(exchangeRateGateway.getOfficialCurrencyName("BRL")).thenReturn(Optional.of("Brazilian Real"));
        when(exchangeRateGateway.getOfficialCurrencyName("PEN")).thenReturn(Optional.of("Peruvian Sol"));
        when(exchangeRateGateway.getOfficialCurrencyName("CHF")).thenReturn(Optional.of("Swiss Franc"));
        when(exchangeRateGateway.getOfficialCurrencyName("KRW")).thenReturn(Optional.of("South Korean Won"));

        // Act
        CurrencyMetadata copMeta = divisasUseCase.consultarMetadatosMoneda(" cop ");
        CurrencyMetadata usdMeta = divisasUseCase.consultarMetadatosMoneda("USD");
        CurrencyMetadata eurMeta = divisasUseCase.consultarMetadatosMoneda("EUR");
        CurrencyMetadata mxnMeta = divisasUseCase.consultarMetadatosMoneda("MXN");
        CurrencyMetadata gbpMeta = divisasUseCase.consultarMetadatosMoneda("GBP");
        CurrencyMetadata jpyMeta = divisasUseCase.consultarMetadatosMoneda("JPY");
        CurrencyMetadata brlMeta = divisasUseCase.consultarMetadatosMoneda("BRL");
        CurrencyMetadata penMeta = divisasUseCase.consultarMetadatosMoneda("PEN");
        CurrencyMetadata chfMeta = divisasUseCase.consultarMetadatosMoneda("CHF");
        CurrencyMetadata krwMeta = divisasUseCase.consultarMetadatosMoneda("KRW");

        // Assert
        assertNotNull(copMeta);
        assertEquals("COP", copMeta.code());
        assertEquals("Colombian Peso", copMeta.name());
        assertEquals("$", copMeta.symbol());
        assertEquals(List.of("Colombia"), copMeta.countries());

        assertEquals("USD", usdMeta.code());
        assertEquals("United States Dollar", usdMeta.name());
        assertEquals("$", usdMeta.symbol());
        assertEquals(List.of("United States"), usdMeta.countries());

        assertEquals("€", eurMeta.symbol());
        assertEquals(List.of("European Union"), eurMeta.countries());
        assertEquals(List.of("Mexico"), mxnMeta.countries());
        assertEquals("£", gbpMeta.symbol());
        assertEquals("¥", jpyMeta.symbol());
        assertEquals("R$", brlMeta.symbol());
        assertEquals("S/", penMeta.symbol());
        assertEquals("Fr", chfMeta.symbol());
        assertEquals("KRW", krwMeta.symbol());
        assertEquals(List.of("International"), krwMeta.countries());
        verifyNoInteractions(currencyMetadataGateway);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenCurrencyCodeIsInvalidOrNotSupportedByExchangeRateApi() {
        // Arrange
        when(exchangeRateGateway.getOfficialCurrencyName("XXX")).thenReturn(Optional.empty());

        // Act
        IllegalArgumentException exFormat = assertThrows(
                IllegalArgumentException.class,
                () -> divisasUseCase.consultarMetadatosMoneda("12")
        );
        IllegalArgumentException exUnsupported = assertThrows(
                IllegalArgumentException.class,
                () -> divisasUseCase.consultarMetadatosMoneda("XXX")
        );

        // Assert
        assertEquals("El código de moneda debe ser un código ISO de 3 letras", exFormat.getMessage());
        assertEquals("Moneda no soportada por ExchangeRate-API: XXX", exUnsupported.getMessage());
        verifyNoInteractions(currencyMetadataGateway);
    }

    private Transaccion buildTransaction(
            String userId,
            BigDecimal amount,
            String currency,
            TransaccionType type,
            FrecuenciaType frequency
    ) {
        return new Transaccion(
                UUID.randomUUID().toString(),
                userId,
                UUID.randomUUID().toString(),
                amount,
                currency,
                type,
                frequency,
                "Desc",
                LocalDate.now(),
                LocalDateTime.now()
        );
    }
}
