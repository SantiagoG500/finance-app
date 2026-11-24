package com.finance.transaction.domain.usecase;

import com.finance.transaction.domain.model.Categoria;
import com.finance.transaction.domain.model.MetaAhorro;
import com.finance.transaction.domain.model.ResumenFinanciero;
import com.finance.transaction.domain.model.Transaccion;
import com.finance.transaction.domain.model.VerifiedUserSession;
import com.finance.transaction.domain.model.enumns.FrecuenciaType;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import com.finance.transaction.domain.model.gateway.AccountVerificationGateway;
import com.finance.transaction.domain.model.gateway.CategoriaGateway;
import com.finance.transaction.domain.model.gateway.ExchangeRateGateway;
import com.finance.transaction.domain.model.gateway.MetaAhorrosGateway;
import com.finance.transaction.domain.model.gateway.NotificationGateway;
import com.finance.transaction.domain.model.gateway.TransaccionGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProyeccionesUseCaseTest {

    @Mock
    private TransaccionGateway transaccionGateway;

    @Mock
    private MetaAhorrosGateway metaAhorrosGateway;

    @Mock
    private NotificationGateway notificationGateway;

    @Mock
    private ExchangeRateGateway exchangeRateGateway;

    @Mock
    private AccountVerificationGateway accountVerificationGateway;

    @Mock
    private CategoriaGateway categoriaGateway;

    private ProyeccionesUseCase proyeccionesUseCase;

    @BeforeEach
    void setUp() {
        proyeccionesUseCase = new ProyeccionesUseCase(
                transaccionGateway,
                metaAhorrosGateway,
                notificationGateway,
                exchangeRateGateway,
                accountVerificationGateway,
                categoriaGateway
        );
        lenient().when(accountVerificationGateway.verifyLoggedUser(anyString(), any()))
                .thenAnswer(inv -> new VerifiedUserSession(inv.getArgument(0), "Test User", "jwt-token"));
    }

    @Test
    void shouldNormalizeMonthlyAmountAcrossAllFrequenciesAndStringOverload() {
        // Arrange
        BigDecimal dailyAmount = new BigDecimal("10.00");
        BigDecimal weeklyAmount = new BigDecimal("100.00");
        BigDecimal monthlyAmount = new BigDecimal("500.00");
        BigDecimal yearlyAmount = new BigDecimal("1200.00");

        // Act
        BigDecimal fromDaily = proyeccionesUseCase.normalizarMontoMensual(dailyAmount, FrecuenciaType.DAILY);
        BigDecimal fromWeekly = proyeccionesUseCase.normalizarMontoMensual(weeklyAmount, FrecuenciaType.WEEKLY);
        BigDecimal fromMonthly = proyeccionesUseCase.normalizarMontoMensual(monthlyAmount, FrecuenciaType.MONTHLY);
        BigDecimal fromYearly = proyeccionesUseCase.normalizarMontoMensual(yearlyAmount, FrecuenciaType.YEARLY);
        BigDecimal fromOneTime = proyeccionesUseCase.normalizarMontoMensual(monthlyAmount, FrecuenciaType.ONE_TIME);
        BigDecimal fromNull = proyeccionesUseCase.normalizarMontoMensual(null, FrecuenciaType.MONTHLY);
        BigDecimal fromStringValid = proyeccionesUseCase.normalizarMontoMensual(monthlyAmount, " monthly ");
        BigDecimal fromStringInvalid = proyeccionesUseCase.normalizarMontoMensual(monthlyAmount, "INVALID");
        BigDecimal fromStringBlank = proyeccionesUseCase.normalizarMontoMensual(monthlyAmount, "  ");

        // Assert
        assertEquals(new BigDecimal("304.10"), fromDaily);
        assertEquals(new BigDecimal("433.00"), fromWeekly);
        assertEquals(new BigDecimal("500.00"), fromMonthly);
        assertEquals(new BigDecimal("100.00"), fromYearly);
        assertEquals(new BigDecimal("0.00"), fromOneTime);
        assertEquals(new BigDecimal("0.00"), fromNull);
        assertEquals(new BigDecimal("500.00"), fromStringValid);
        assertEquals(new BigDecimal("0.00"), fromStringInvalid);
        assertEquals(new BigDecimal("0.00"), fromStringBlank);
    }

    @Test
    void shouldCalculateDirectListAggregationsForIncomesExpensesGoalsAndInitialBalance() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        List<Transaccion> transacciones = List.of(
                buildTransaction(userId, new BigDecimal("1000.00"), "USD", TransaccionType.INCOME, FrecuenciaType.MONTHLY),
                buildTransaction(userId, new BigDecimal("300.00"), "USD", TransaccionType.EXPENSE, FrecuenciaType.MONTHLY),
                buildTransaction(userId, new BigDecimal("500.00"), "USD", TransaccionType.INCOME, FrecuenciaType.ONE_TIME),
                buildTransaction(userId, new BigDecimal("100.00"), " ", TransaccionType.EXPENSE, FrecuenciaType.ONE_TIME)
        );
        List<MetaAhorro> metas = List.of(buildGoal(userId, new BigDecimal("250.00"), "USD"));

        // Act
        BigDecimal incomes = proyeccionesUseCase.calcularIngresosMensuales(transacciones);
        BigDecimal incomesEmpty = proyeccionesUseCase.calcularIngresosMensuales(Collections.emptyList());
        BigDecimal expenses = proyeccionesUseCase.calcularGastosMensuales(transacciones);
        BigDecimal expensesEmpty = proyeccionesUseCase.calcularGastosMensuales(Collections.emptyList());
        BigDecimal savings = proyeccionesUseCase.calcularCompromisoAhorroMensual(metas);
        BigDecimal savingsEmpty = proyeccionesUseCase.calcularCompromisoAhorroMensual(Collections.emptyList());
        BigDecimal initialBalance = proyeccionesUseCase.calcularSaldoInicialAcumulado(transacciones);
        BigDecimal initialEmpty = proyeccionesUseCase.calcularSaldoInicialAcumulado(Collections.emptyList());

        // Assert
        assertEquals(new BigDecimal("1000.00"), incomes);
        assertEquals(new BigDecimal("0.00"), incomesEmpty);
        assertEquals(new BigDecimal("300.00"), expenses);
        assertEquals(new BigDecimal("0.00"), expensesEmpty);
        assertEquals(new BigDecimal("250.00"), savings);
        assertEquals(new BigDecimal("0.00"), savingsEmpty);
        assertEquals(new BigDecimal("400.00"), initialBalance);
        assertEquals(new BigDecimal("0.00"), initialEmpty);
    }

    @Test
    void shouldCalculateFinancialSummaryWithAllFrequenciesSavingsGoalsAndCategoryBreakdown() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        String catSalarioId = UUID.randomUUID().toString();
        String catComidaId = UUID.randomUUID().toString();

        Transaccion txSalario1 = buildTransaction(userId, new BigDecimal("3000.00"), "COP", TransaccionType.INCOME, FrecuenciaType.MONTHLY);
        txSalario1.setCategoryId(catSalarioId);
        Transaccion txSalario2 = buildTransaction(userId, new BigDecimal("1000.00"), "COP", TransaccionType.INCOME, FrecuenciaType.ONE_TIME);
        txSalario2.setCategoryId(catSalarioId);
        Transaccion txComida = buildTransaction(userId, new BigDecimal("200.00"), "COP", TransaccionType.EXPENSE, FrecuenciaType.ONE_TIME);
        txComida.setCategoryId(catComidaId);
        Transaccion txSinCategoria = buildTransaction(userId, new BigDecimal("10.00"), "COP", TransaccionType.EXPENSE, FrecuenciaType.DAILY);
        txSinCategoria.setCategoryId(null);
        Transaccion txWeekly = buildTransaction(userId, new BigDecimal("100.00"), "COP", TransaccionType.EXPENSE, FrecuenciaType.WEEKLY);
        txWeekly.setCategoryId(catComidaId);
        Transaccion txYearly = buildTransaction(userId, new BigDecimal("1200.00"), "COP", TransaccionType.EXPENSE, FrecuenciaType.YEARLY);

        List<Transaccion> transacciones = List.of(
                txSalario1,
                txSalario2,
                txComida,
                txSinCategoria,
                txWeekly,
                txYearly
        );
        MetaAhorro goal1 = buildGoal(userId, new BigDecimal("300.00"), "COP");
        MetaAhorro goal2 = buildGoal(userId, BigDecimal.ZERO, "COP");

        List<Categoria> categorias = List.of(
                new Categoria(catSalarioId, userId, "Salario", TransaccionType.INCOME),
                new Categoria(catComidaId, userId, "Alimentación", TransaccionType.EXPENSE)
        );

        when(transaccionGateway.obtenerTransaccionesPorUsuario(userId)).thenReturn(transacciones);
        when(metaAhorrosGateway.obtenerMetasPorUsuario(userId)).thenReturn(List.of(goal1, goal2));
        when(categoriaGateway.obtenerCategoriasPorUsuario(userId)).thenReturn(categorias);

        // Act
        ResumenFinanciero resumen = proyeccionesUseCase.obtenerResumenFinanciero(userId, "Bearer jwt-token");

        // Assert
        assertEquals(userId, resumen.getUsuarioId());
        assertEquals("COP", resumen.getMoneda());
        assertEquals(new BigDecimal("3000.00"), resumen.getIngresosMensuales());
        assertEquals(new BigDecimal("837.10"), resumen.getGastosMensuales());
        assertEquals(new BigDecimal("2162.90"), resumen.getBalanceNetoMensual());
        assertEquals(new BigDecimal("300.00"), resumen.getCompromisoAhorroMensual());
        assertEquals(new BigDecimal("1862.90"), resumen.getMargenLibre());
        assertEquals(new BigDecimal("26754.80"), resumen.getProyeccion1Ano());
        assertNotNull(resumen.getPorCategoria());
        assertEquals(new BigDecimal("4000.00"), resumen.getPorCategoria().get("Salario"));
        assertEquals(new BigDecimal("300.00"), resumen.getPorCategoria().get("Alimentación"));
        assertEquals(new BigDecimal("1210.00"), resumen.getPorCategoria().get("Sin categoría"));
        verify(accountVerificationGateway).verifyLoggedUser(userId, "Bearer jwt-token");
        verify(transaccionGateway, times(1)).obtenerTransaccionesPorUsuario(userId);
        verify(metaAhorrosGateway, times(1)).obtenerMetasPorUsuario(userId);
        verify(categoriaGateway, times(1)).obtenerCategoriasPorUsuario(userId);
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
                () -> proyeccionesUseCase.calcularBalanceNeto(userId, "Bearer bad")
        );
        IllegalStateException exBlank = assertThrows(
                IllegalStateException.class,
                () -> proyeccionesUseCase.obtenerResumenFinanciero(userId, "Bearer bad")
        );

        // Assert
        assertEquals("La sesión del usuario no pudo ser verificada", exNull.getMessage());
        assertEquals("La sesión del usuario no pudo ser verificada", exBlank.getMessage());
    }

    @Test
    void shouldConsolidateMultiCurrencyTransactionsInSummaryAndNetBalanceUsingPrecalculatedExchangeRates() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        List<Transaccion> transacciones = List.of(
                buildTransaction(userId, new BigDecimal("1000.00"), "USD", TransaccionType.INCOME, FrecuenciaType.MONTHLY),
                buildTransaction(userId, new BigDecimal("1000000.00"), "COP", TransaccionType.INCOME, FrecuenciaType.MONTHLY),
                buildTransaction(userId, new BigDecimal("500000.00"), "COP", TransaccionType.INCOME, FrecuenciaType.MONTHLY),
                buildTransaction(userId, new BigDecimal("200.00"), "EUR", TransaccionType.EXPENSE, FrecuenciaType.MONTHLY)
        );
        MetaAhorro copGoal = buildGoal(userId, new BigDecimal("400000.00"), "COP");

        when(transaccionGateway.obtenerTransaccionesPorUsuario(userId)).thenReturn(transacciones);
        when(metaAhorrosGateway.obtenerMetasPorUsuario(userId)).thenReturn(List.of(copGoal));
        when(exchangeRateGateway.getExchangeRate("COP", "USD")).thenReturn(new BigDecimal("0.00025"));
        when(exchangeRateGateway.getExchangeRate("EUR", "USD")).thenReturn(new BigDecimal("1.10"));

        // Act
        ResumenFinanciero resumen = proyeccionesUseCase.obtenerResumenFinanciero(userId);
        BigDecimal balanceNeto = proyeccionesUseCase.calcularBalanceNeto(userId);

        // Assert
        assertEquals("USD", resumen.getMoneda());
        assertEquals(new BigDecimal("1375.00"), resumen.getIngresosMensuales());
        assertEquals(new BigDecimal("220.00"), resumen.getGastosMensuales());
        assertEquals(new BigDecimal("1155.00"), resumen.getBalanceNetoMensual());
        assertEquals(new BigDecimal("100.00"), resumen.getCompromisoAhorroMensual());
        assertEquals(new BigDecimal("1055.00"), resumen.getMargenLibre());
        assertEquals(new BigDecimal("1155.00"), balanceNeto);
        verify(exchangeRateGateway, times(2)).getExchangeRate("COP", "USD");
        verify(exchangeRateGateway, times(2)).getExchangeRate("EUR", "USD");
    }

    @Test
    void shouldCalculateFinancialSummaryWithEmptyOrNullListsAndFallbackCurrency() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        MetaAhorro euroGoal = buildGoal(userId, new BigDecimal("150.00"), "EUR");

        when(transaccionGateway.obtenerTransaccionesPorUsuario(userId))
                .thenReturn(null)
                .thenReturn(Collections.emptyList());
        when(metaAhorrosGateway.obtenerMetasPorUsuario(userId))
                .thenReturn(List.of(euroGoal))
                .thenReturn(null);

        // Act
        ResumenFinanciero resumenWithGoalCurrency = proyeccionesUseCase.obtenerResumenFinanciero(userId);
        ResumenFinanciero resumenWithDefaultUsd = proyeccionesUseCase.obtenerResumenFinanciero(userId);

        // Assert
        assertEquals("EUR", resumenWithGoalCurrency.getMoneda());
        assertEquals(new BigDecimal("0.00"), resumenWithGoalCurrency.getBalanceNetoMensual());
        assertEquals("USD", resumenWithDefaultUsd.getMoneda());
        assertEquals(new BigDecimal("0.00"), resumenWithDefaultUsd.getCompromisoAhorroMensual());
    }

    @Test
    void shouldCalculateNetBalanceByUserIdSuccessfully() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        List<Transaccion> transacciones = List.of(
                buildTransaction(userId, new BigDecimal("2000.00"), "USD", TransaccionType.INCOME, FrecuenciaType.MONTHLY),
                buildTransaction(userId, new BigDecimal("750.00"), "USD", TransaccionType.EXPENSE, FrecuenciaType.MONTHLY)
        );
        when(transaccionGateway.obtenerTransaccionesPorUsuario(userId)).thenReturn(transacciones);

        // Act
        BigDecimal netBalance = proyeccionesUseCase.calcularBalanceNeto(userId);

        // Assert
        assertEquals(new BigDecimal("1250.00"), netBalance);
    }

    @Test
    void shouldProjectPositiveBalanceWithoutTriggeringDeficitAlert() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        List<Transaccion> transacciones = List.of(
                buildTransaction(userId, new BigDecimal("1000.00"), "USD", TransaccionType.INCOME, FrecuenciaType.MONTHLY),
                buildTransaction(userId, new BigDecimal("400.00"), "USD", TransaccionType.EXPENSE, FrecuenciaType.MONTHLY)
        );
        when(transaccionGateway.obtenerTransaccionesPorUsuario(userId)).thenReturn(transacciones);

        // Act
        BigDecimal projected = proyeccionesUseCase.proyectarSaldo(userId, 6, "user@finance.com");

        // Assert
        assertEquals(new BigDecimal("3600.00"), projected);
        verifyNoInteractions(notificationGateway);
    }

    @Test
    void shouldProjectNegativeBalanceAndTriggerDeficitAlert() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        List<Transaccion> transacciones = List.of(
                buildTransaction(userId, new BigDecimal("500.00"), "USD", TransaccionType.INCOME, FrecuenciaType.MONTHLY),
                buildTransaction(userId, new BigDecimal("900.00"), "USD", TransaccionType.EXPENSE, FrecuenciaType.MONTHLY)
        );
        when(transaccionGateway.obtenerTransaccionesPorUsuario(userId)).thenReturn(transacciones);

        // Act
        BigDecimal projected = proyeccionesUseCase.proyectarSaldo(userId, 3, "user@finance.com");

        // Assert
        assertEquals(new BigDecimal("-1200.00"), projected);
        verify(notificationGateway).notifyProjectionDeficit("user@finance.com", "3", "-1200.00");
    }

    @Test
    void shouldCompleteNegativeProjectionWhenNotificationGatewayThrowsException() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        List<Transaccion> transacciones = List.of(
                buildTransaction(userId, new BigDecimal("100.00"), "USD", TransaccionType.EXPENSE, FrecuenciaType.MONTHLY)
        );
        when(transaccionGateway.obtenerTransaccionesPorUsuario(userId)).thenReturn(transacciones);
        doThrow(new RuntimeException("Notification down"))
                .when(notificationGateway)
                .notifyProjectionDeficit("user@finance.com", "2", "-200.00");

        // Act
        BigDecimal projected = proyeccionesUseCase.proyectarSaldo(userId, 2, "user@finance.com");

        // Assert
        assertEquals(new BigDecimal("-200.00"), projected);
        verify(notificationGateway).notifyProjectionDeficit("user@finance.com", "2", "-200.00");
    }

    @Test
    void shouldProjectUsingTwoArgThreeArgFourArgAndFiveArgConstructorsWithoutExchangeRateOrNotification() {
        // Arrange
        ProyeccionesUseCase useCaseTwoArgs = new ProyeccionesUseCase(transaccionGateway, metaAhorrosGateway);
        ProyeccionesUseCase useCaseThreeArgs = new ProyeccionesUseCase(transaccionGateway, metaAhorrosGateway, notificationGateway);
        ProyeccionesUseCase useCaseFourArgs = new ProyeccionesUseCase(transaccionGateway, metaAhorrosGateway, notificationGateway, exchangeRateGateway);
        ProyeccionesUseCase useCaseFiveArgs = new ProyeccionesUseCase(
                transaccionGateway,
                metaAhorrosGateway,
                notificationGateway,
                exchangeRateGateway,
                accountVerificationGateway
        );
        String userId = UUID.randomUUID().toString();
        List<Transaccion> transacciones = List.of(
                buildTransaction(userId, new BigDecimal("100.00"), "USD", TransaccionType.EXPENSE, FrecuenciaType.MONTHLY)
        );
        when(transaccionGateway.obtenerTransaccionesPorUsuario(userId)).thenReturn(transacciones);

        // Act
        BigDecimal result1 = proyeccionesUseCase.proyectarSaldo(userId, 2);
        BigDecimal result2 = useCaseTwoArgs.proyectarSaldo(userId, 2, "user@finance.com");
        BigDecimal result3 = useCaseThreeArgs.calcularBalanceNeto(userId);
        BigDecimal result4 = useCaseFourArgs.calcularBalanceNeto(userId);
        BigDecimal result5 = useCaseFiveArgs.calcularBalanceNeto(userId);
        BigDecimal resultNullInputs = proyeccionesUseCase.proyectarSaldo(null, null, 5);

        // Assert
        assertEquals(new BigDecimal("-200.00"), result1);
        assertEquals(new BigDecimal("-200.00"), result2);
        assertEquals(new BigDecimal("-100.00"), result3);
        assertEquals(new BigDecimal("-100.00"), result4);
        assertEquals(new BigDecimal("-100.00"), result5);
        assertEquals(new BigDecimal("0.00"), resultNullInputs);
        verifyNoInteractions(notificationGateway);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenExternalUserIdIsNullOrBlank() {
        // Arrange
        String blankUserId = "  ";

        // Act
        IllegalArgumentException exNull = assertThrows(
                IllegalArgumentException.class,
                () -> proyeccionesUseCase.calcularBalanceNeto((String) null)
        );
        IllegalArgumentException exBlank = assertThrows(
                IllegalArgumentException.class,
                () -> proyeccionesUseCase.obtenerResumenFinanciero(blankUserId)
        );

        // Assert
        assertEquals("El ID externo del usuario no puede ser nulo o vacío.", exNull.getMessage());
        assertEquals("El ID externo del usuario no puede ser nulo o vacío.", exBlank.getMessage());
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

    private MetaAhorro buildGoal(String userId, BigDecimal monthlyContribution, String currency) {
        return new MetaAhorro(
                UUID.randomUUID().toString(),
                userId,
                "Viaje",
                new BigDecimal("5000.00"),
                new BigDecimal("500.00"),
                monthlyContribution,
                currency,
                LocalDate.now().plusYears(1),
                LocalDateTime.now()
        );
    }
}
