package com.finance.transaction.domain.usecase;

import com.finance.transaction.domain.model.MetaAhorro;
import com.finance.transaction.domain.model.ResumenFinanciero;
import com.finance.transaction.domain.model.Transaccion;
import com.finance.transaction.domain.model.enumns.FrecuenciaType;
import com.finance.transaction.domain.model.enumns.TransaccionType;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProyeccionesUseCaseTest {

    @Mock
    private TransaccionGateway transaccionGateway;

    @Mock
    private MetaAhorrosGateway metaAhorrosGateway;

    @Mock
    private NotificationGateway notificationGateway;

    private ProyeccionesUseCase proyeccionesUseCase;

    @BeforeEach
    void setUp() {
        proyeccionesUseCase = new ProyeccionesUseCase(transaccionGateway, metaAhorrosGateway, notificationGateway);
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
    void shouldCalculateFinancialSummaryWithAllFrequenciesAndSavingsGoals() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        List<Transaccion> transacciones = List.of(
                buildTransaction(userId, new BigDecimal("3000.00"), "COP", TransaccionType.INCOME, FrecuenciaType.MONTHLY),
                buildTransaction(userId, new BigDecimal("1000.00"), "COP", TransaccionType.INCOME, FrecuenciaType.ONE_TIME),
                buildTransaction(userId, new BigDecimal("200.00"), "COP", TransaccionType.EXPENSE, FrecuenciaType.ONE_TIME),
                buildTransaction(userId, new BigDecimal("10.00"), "COP", TransaccionType.EXPENSE, FrecuenciaType.DAILY),
                buildTransaction(userId, new BigDecimal("100.00"), "COP", TransaccionType.EXPENSE, FrecuenciaType.WEEKLY),
                buildTransaction(userId, new BigDecimal("1200.00"), "COP", TransaccionType.EXPENSE, FrecuenciaType.YEARLY)
        );
        MetaAhorro goal1 = buildGoal(userId, new BigDecimal("300.00"), "COP");
        MetaAhorro goal2 = buildGoal(userId, BigDecimal.ZERO, "COP");

        when(transaccionGateway.obtenerTransaccionesPorUsuario(userId)).thenReturn(transacciones);
        when(metaAhorrosGateway.obtenerMetasPorUsuario(userId)).thenReturn(List.of(goal1, goal2));

        // Act
        ResumenFinanciero resumen = proyeccionesUseCase.obtenerResumenFinanciero(userId);

        // Assert
        assertEquals(userId, resumen.getUsuarioId());
        assertEquals("COP", resumen.getMoneda());
        assertEquals(new BigDecimal("3000.00"), resumen.getIngresosMensuales());
        assertEquals(new BigDecimal("837.10"), resumen.getGastosMensuales());
        assertEquals(new BigDecimal("2162.90"), resumen.getBalanceNetoMensual());
        assertEquals(new BigDecimal("300.00"), resumen.getCompromisoAhorroMensual());
        assertEquals(new BigDecimal("1862.90"), resumen.getMargenLibre());
        assertEquals(new BigDecimal("26754.80"), resumen.getProyeccion1Ano());
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
    void shouldProjectUsingTwoArgOverloadAndTwoArgConstructorWithoutNotification() {
        // Arrange
        ProyeccionesUseCase useCaseWithoutNotification = new ProyeccionesUseCase(transaccionGateway, metaAhorrosGateway);
        String userId = UUID.randomUUID().toString();
        List<Transaccion> transacciones = List.of(
                buildTransaction(userId, new BigDecimal("100.00"), "USD", TransaccionType.EXPENSE, FrecuenciaType.MONTHLY)
        );
        when(transaccionGateway.obtenerTransaccionesPorUsuario(userId)).thenReturn(transacciones);

        // Act
        BigDecimal result1 = proyeccionesUseCase.proyectarSaldo(userId, 2);
        BigDecimal result2 = useCaseWithoutNotification.proyectarSaldo(userId, 2, "user@finance.com");
        BigDecimal resultNullInputs = proyeccionesUseCase.proyectarSaldo(null, null, 5);

        // Assert
        assertEquals(new BigDecimal("-200.00"), result1);
        assertEquals(new BigDecimal("-200.00"), result2);
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
