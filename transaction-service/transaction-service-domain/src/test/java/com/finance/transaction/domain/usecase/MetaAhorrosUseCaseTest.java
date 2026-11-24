package com.finance.transaction.domain.usecase;

import com.finance.transaction.domain.model.MetaAhorro;
import com.finance.transaction.domain.model.PaginatedMetaAhorro;
import com.finance.transaction.domain.model.VerifiedUserSession;
import com.finance.transaction.domain.model.gateway.AccountVerificationGateway;
import com.finance.transaction.domain.model.gateway.MetaAhorrosGateway;
import com.finance.transaction.domain.model.gateway.NotificationGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetaAhorrosUseCaseTest {

    @Mock
    private MetaAhorrosGateway metaAhorrosGateway;

    @Mock
    private NotificationGateway notificationGateway;

    @Mock
    private AccountVerificationGateway accountVerificationGateway;

    private MetaAhorrosUseCase metaAhorrosUseCase;

    @BeforeEach
    void setUp() {
        metaAhorrosUseCase = new MetaAhorrosUseCase(
                metaAhorrosGateway,
                notificationGateway,
                accountVerificationGateway
        );
        lenient().when(accountVerificationGateway.verifyLoggedUser(anyString(), any()))
                .thenAnswer(inv -> new VerifiedUserSession(inv.getArgument(0), "Test User", "jwt-token"));
    }

    @Test
    void shouldSaveSavingsGoalSuccessfullyAndGenerateIdAndTimestampWhenMissing() {
        // Arrange
        MetaAhorro goal = buildValidGoal(new BigDecimal("100.00"), new BigDecimal("1000.00"));
        goal.setId(null);
        goal.setCreatedAt(null);
        when(metaAhorrosGateway.guardarMetaAhorros(any(MetaAhorro.class))).thenReturn(goal);

        // Act
        MetaAhorro saved = metaAhorrosUseCase.guardarMetaAhorros(goal, "Bearer jwt-token");

        // Assert
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        verify(accountVerificationGateway).verifyLoggedUser(goal.getExternalUserId(), "Bearer jwt-token");
        verify(metaAhorrosGateway).guardarMetaAhorros(goal);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenAccountVerificationReturnsNullOrBlankSessionId() {
        // Arrange
        MetaAhorro goal = buildValidGoal(new BigDecimal("100.00"), new BigDecimal("1000.00"));
        when(accountVerificationGateway.verifyLoggedUser(goal.getExternalUserId(), "Bearer bad"))
                .thenReturn(null)
                .thenReturn(new VerifiedUserSession(" ", "User", "bad"));

        // Act
        IllegalStateException exNull = assertThrows(
                IllegalStateException.class,
                () -> metaAhorrosUseCase.guardarMetaAhorros(goal, "Bearer bad")
        );
        IllegalStateException exBlank = assertThrows(
                IllegalStateException.class,
                () -> metaAhorrosUseCase.guardarMetaAhorros(goal, "Bearer bad")
        );

        // Assert
        assertEquals("La sesión del usuario no pudo ser verificada", exNull.getMessage());
        assertEquals("La sesión del usuario no pudo ser verificada", exBlank.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenSavingNullOrInvalidSavingsGoal() {
        // Arrange
        MetaAhorro noUser = buildValidGoal(BigDecimal.ZERO, new BigDecimal("500.00"));
        noUser.setExternalUserId(" ");
        MetaAhorro noTitle = buildValidGoal(BigDecimal.ZERO, new BigDecimal("500.00"));
        noTitle.setTitle("");
        MetaAhorro invalidTarget = buildValidGoal(BigDecimal.ZERO, BigDecimal.ZERO);
        MetaAhorro noCurrency = buildValidGoal(BigDecimal.ZERO, new BigDecimal("500.00"));
        noCurrency.setCurrency(null);
        MetaAhorro noTargetDate = buildValidGoal(BigDecimal.ZERO, new BigDecimal("500.00"));
        noTargetDate.setTargetDate(null);
        MetaAhorro negativeCurrent = buildValidGoal(new BigDecimal("-1.00"), new BigDecimal("500.00"));
        MetaAhorro negativeMonthly = buildValidGoal(BigDecimal.ZERO, new BigDecimal("500.00"));
        negativeMonthly.setMonthlyContribution(new BigDecimal("-10.00"));

        // Act
        IllegalArgumentException exNullSave = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosUseCase.guardarMetaAhorros(null)
        );
        IllegalArgumentException exNullObj = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosUseCase.validarMetaAhorrosObj(null)
        );
        IllegalArgumentException exUser = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosUseCase.guardarMetaAhorros(noUser)
        );
        IllegalArgumentException exTitle = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosUseCase.guardarMetaAhorros(noTitle)
        );
        IllegalArgumentException exTarget = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosUseCase.guardarMetaAhorros(invalidTarget)
        );
        IllegalArgumentException exCurrency = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosUseCase.guardarMetaAhorros(noCurrency)
        );
        IllegalArgumentException exDate = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosUseCase.guardarMetaAhorros(noTargetDate)
        );
        IllegalArgumentException exCurrent = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosUseCase.guardarMetaAhorros(negativeCurrent)
        );
        IllegalArgumentException exMonthly = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosUseCase.guardarMetaAhorros(negativeMonthly)
        );

        // Assert
        assertEquals("La meta de ahorros no puede ser nula.", exNullSave.getMessage());
        assertEquals("La meta de ahorros no puede ser nula.", exNullObj.getMessage());
        assertEquals("El usuario externo asociado no puede ser nulo o vacío.", exUser.getMessage());
        assertEquals("El título de la meta no puede ser nulo o vacío.", exTitle.getMessage());
        assertEquals("El monto objetivo debe ser mayor a cero.", exTarget.getMessage());
        assertEquals("La moneda (currency) no puede ser nula o vacía.", exCurrency.getMessage());
        assertEquals("La fecha objetivo no puede ser nula.", exDate.getMessage());
        assertEquals("El monto actual no puede ser negativo.", exCurrent.getMessage());
        assertEquals("El aporte mensual planificado no puede ser negativo.", exMonthly.getMessage());
    }

    @Test
    void shouldGetGoalByIdAndByUserSuccessfully() {
        // Arrange
        MetaAhorro goal = buildValidGoal(new BigDecimal("200.00"), new BigDecimal("1000.00"));
        when(metaAhorrosGateway.obtenerMetaAhorros(goal.getId(), goal.getExternalUserId())).thenReturn(goal);
        when(metaAhorrosGateway.obtenerMetasPorUsuario(goal.getExternalUserId())).thenReturn(List.of(goal));

        // Act
        MetaAhorro found = metaAhorrosUseCase.obtenerMetaAhorros(goal.getId(), goal.getExternalUserId());
        List<MetaAhorro> list = metaAhorrosUseCase.obtenerMetasPorUsuario(goal.getExternalUserId());

        // Assert
        assertNotNull(found);
        assertEquals(1, list.size());
        assertEquals(goal.getTitle(), list.get(0).getTitle());
    }

    @Test
    void shouldGetPaginatedGoalsByUserSuccessfullyWhenParametersAreValid() {
        // Arrange
        MetaAhorro goal = buildValidGoal(new BigDecimal("200.00"), new BigDecimal("1000.00"));
        String userId = goal.getExternalUserId();
        PaginatedMetaAhorro paginated = new PaginatedMetaAhorro(List.of(goal), 0, 10, 1L, 1, true);
        when(metaAhorrosGateway.obtenerMetasPaginadasPorUsuario(userId, 0, 10)).thenReturn(paginated);

        // Act
        PaginatedMetaAhorro result = metaAhorrosUseCase.obtenerMetasPaginadas(userId, "Bearer jwt-token", 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(1L, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.last());
        verify(accountVerificationGateway).verifyLoggedUser(userId, "Bearer jwt-token");
        verify(metaAhorrosGateway).obtenerMetasPaginadasPorUsuario(userId, 0, 10);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenGetPaginatedGoalsReceivesInvalidPageSizeOrUserId() {
        // Arrange
        String userId = UUID.randomUUID().toString();

        // Act
        IllegalArgumentException exUser = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosUseCase.obtenerMetasPaginadas("  ", "Bearer jwt", 0, 10)
        );
        IllegalArgumentException exNegativePage = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosUseCase.obtenerMetasPaginadas(userId, "Bearer jwt", -1, 10)
        );
        IllegalArgumentException exZeroSize = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosUseCase.obtenerMetasPaginadas(userId, "Bearer jwt", 0, 0)
        );
        IllegalArgumentException exTooLargeSize = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosUseCase.obtenerMetasPaginadas(userId, "Bearer jwt", 0, 101)
        );

        // Assert
        assertEquals("El ID externo del usuario no puede ser nulo o vacío.", exUser.getMessage());
        assertEquals("El número de página (page) debe ser mayor o igual a 0.", exNegativePage.getMessage());
        assertEquals("El tamaño de página (size) debe estar entre 1 y 100.", exZeroSize.getMessage());
        assertEquals("El tamaño de página (size) debe estar entre 1 y 100.", exTooLargeSize.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenGetGoalsByUserHasInvalidUserId() {
        // Arrange
        String blankUserId = "  ";

        // Act
        IllegalArgumentException exNull = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosUseCase.obtenerMetasPorUsuario(null)
        );
        IllegalArgumentException exBlank = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosUseCase.obtenerMetasPorUsuario(blankUserId)
        );

        // Assert
        assertEquals("El ID externo del usuario no puede ser nulo o vacío.", exNull.getMessage());
        assertEquals("El ID externo del usuario no puede ser nulo o vacío.", exBlank.getMessage());
    }

    @Test
    void shouldUpdateAndDeleteSavingsGoalSuccessfully() {
        // Arrange
        MetaAhorro goal = buildValidGoal(new BigDecimal("300.00"), new BigDecimal("1000.00"));
        when(metaAhorrosGateway.actualizarMetaAhorros(goal)).thenReturn(goal);

        // Act
        MetaAhorro updated = metaAhorrosUseCase.actualizarMetaAhorros(goal);
        metaAhorrosUseCase.eliminarMetaAhorros(goal.getId(), goal.getExternalUserId());

        // Assert
        assertNotNull(updated);
        verify(metaAhorrosGateway).actualizarMetaAhorros(goal);
        verify(metaAhorrosGateway).eliminarMetaAhorros(goal.getId(), goal.getExternalUserId());
    }

    @Test
    void shouldDepositPartialAmountWithoutTriggeringGoalAchievedNotification() {
        // Arrange
        MetaAhorro goal = buildValidGoal(null, new BigDecimal("1000.00"));
        when(metaAhorrosGateway.obtenerMetaAhorros(goal.getId(), goal.getExternalUserId())).thenReturn(goal);
        when(metaAhorrosGateway.actualizarMetaAhorros(goal)).thenReturn(goal);

        // Act
        MetaAhorro updated = metaAhorrosUseCase.abonarMetaAhorros(
                goal.getId(),
                goal.getExternalUserId(),
                new BigDecimal("250.00"),
                "user@finance.com"
        );

        // Assert
        assertEquals(new BigDecimal("250.00"), updated.getCurrentAmount());
        verifyNoInteractions(notificationGateway);
    }

    @Test
    void shouldDepositAndNotifyWhenGoalTargetAmountIsReached() {
        // Arrange
        MetaAhorro goal = buildValidGoal(new BigDecimal("800.00"), new BigDecimal("1000.00"));
        when(metaAhorrosGateway.obtenerMetaAhorros(goal.getId(), goal.getExternalUserId())).thenReturn(goal);
        when(metaAhorrosGateway.actualizarMetaAhorros(goal)).thenReturn(goal);

        // Act
        MetaAhorro updated = metaAhorrosUseCase.abonarMetaAhorros(
                goal.getId(),
                goal.getExternalUserId(),
                new BigDecimal("200.00"),
                "user@finance.com"
        );

        // Assert
        assertEquals(new BigDecimal("1000.00"), updated.getCurrentAmount());
        verify(notificationGateway).notifySavingsGoalAchieved(
                "user@finance.com",
                "Fondo de Emergencia",
                "1000.00 / 1000.00 USD",
                "100.0%"
        );
    }

    @Test
    void shouldCompleteDepositWhenNotificationGatewayThrowsException() {
        // Arrange
        MetaAhorro goal = buildValidGoal(new BigDecimal("900.00"), new BigDecimal("1000.00"));
        when(metaAhorrosGateway.obtenerMetaAhorros(goal.getId(), goal.getExternalUserId())).thenReturn(goal);
        when(metaAhorrosGateway.actualizarMetaAhorros(goal)).thenReturn(goal);
        doThrow(new RuntimeException("Feign error"))
                .when(notificationGateway)
                .notifySavingsGoalAchieved(anyString(), anyString(), anyString(), anyString());

        // Act
        MetaAhorro updated = metaAhorrosUseCase.abonarMetaAhorros(
                goal.getId(),
                goal.getExternalUserId(),
                new BigDecimal("100.00"),
                "user@finance.com"
        );

        // Assert
        assertEquals(new BigDecimal("1000.00"), updated.getCurrentAmount());
        verify(notificationGateway).notifySavingsGoalAchieved(
                "user@finance.com",
                "Fondo de Emergencia",
                "1000.00 / 1000.00 USD",
                "100.0%"
        );
    }

    @Test
    void shouldDepositUsingThreeArgOverloadAndConstructorsWithoutNotification() {
        // Arrange
        MetaAhorrosUseCase useCaseSingleArg = new MetaAhorrosUseCase(metaAhorrosGateway);
        MetaAhorrosUseCase useCaseTwoArgs = new MetaAhorrosUseCase(metaAhorrosGateway, notificationGateway);
        MetaAhorro goal = buildValidGoal(new BigDecimal("850.00"), new BigDecimal("1000.00"));
        when(metaAhorrosGateway.obtenerMetaAhorros(goal.getId(), goal.getExternalUserId())).thenReturn(goal);
        when(metaAhorrosGateway.actualizarMetaAhorros(goal)).thenReturn(goal);

        // Act
        MetaAhorro result1 = metaAhorrosUseCase.abonarMetaAhorros(goal.getId(), goal.getExternalUserId(), new BigDecimal("100.00"));
        MetaAhorro result2 = useCaseSingleArg.abonarMetaAhorros(goal.getId(), goal.getExternalUserId(), new BigDecimal("25.00"), "a@b.com");
        MetaAhorro result3 = useCaseTwoArgs.abonarMetaAhorros(goal.getId(), goal.getExternalUserId(), new BigDecimal("25.00"));

        // Assert
        assertEquals(new BigDecimal("1000.00"), result3.getCurrentAmount());
        assertNotNull(result1);
        assertNotNull(result2);
        verifyNoInteractions(notificationGateway);
    }

    @Test
    void shouldThrowIllegalStateOrIllegalArgumentWhenGoalAlreadyCompletedOrDepositExceedsRemaining() {
        // Arrange
        MetaAhorro completedGoal = buildValidGoal(new BigDecimal("1000.00"), new BigDecimal("1000.00"));
        MetaAhorro partialGoal = buildValidGoal(new BigDecimal("900.00"), new BigDecimal("1000.00"));
        when(metaAhorrosGateway.obtenerMetaAhorros(completedGoal.getId(), completedGoal.getExternalUserId()))
                .thenReturn(completedGoal);
        when(metaAhorrosGateway.obtenerMetaAhorros(partialGoal.getId(), partialGoal.getExternalUserId()))
                .thenReturn(partialGoal);

        // Act
        IllegalStateException exCompleted = assertThrows(
                IllegalStateException.class,
                () -> metaAhorrosUseCase.abonarMetaAhorros(completedGoal.getId(), completedGoal.getExternalUserId(), new BigDecimal("50.00"))
        );
        IllegalArgumentException exExceeds = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosUseCase.abonarMetaAhorros(partialGoal.getId(), partialGoal.getExternalUserId(), new BigDecimal("150.00"))
        );

        // Assert
        assertEquals("La meta de ahorro ya alcanzó el 100% de su monto objetivo", exCompleted.getMessage());
        assertEquals("El monto del abono supera el saldo restante para completar la meta", exExceeds.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenDepositAmountIsNullOrNonPositive() {
        // Arrange
        String goalId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        // Act
        IllegalArgumentException exNull = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosUseCase.abonarMetaAhorros(goalId, userId, null, "a@b.com")
        );
        IllegalArgumentException exZero = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosUseCase.abonarMetaAhorros(goalId, userId, BigDecimal.ZERO, "a@b.com")
        );
        IllegalArgumentException exBadId = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosUseCase.validarParametros("", userId)
        );
        IllegalArgumentException exBadUser = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosUseCase.validarParametros(goalId, " ")
        );

        // Assert
        assertEquals("El monto del abono debe ser mayor a cero", exNull.getMessage());
        assertEquals("El monto del abono debe ser mayor a cero", exZero.getMessage());
        assertEquals("El ID de la meta de ahorro no puede ser nulo o vacío.", exBadId.getMessage());
        assertEquals("El ID externo del usuario no puede ser nulo o vacío.", exBadUser.getMessage());
    }

    @Test
    void shouldThrowNoSuchElementExceptionWhenDepositingToNonExistentGoal() {
        // Arrange
        String goalId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        when(metaAhorrosGateway.obtenerMetaAhorros(goalId, userId)).thenReturn(null);

        // Act
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> metaAhorrosUseCase.abonarMetaAhorros(goalId, userId, new BigDecimal("50.00"), "user@finance.com")
        );

        // Assert
        assertEquals("Meta de ahorro no encontrada", exception.getMessage());
    }

    private MetaAhorro buildValidGoal(BigDecimal currentAmount, BigDecimal targetAmount) {
        return new MetaAhorro(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "Fondo de Emergencia",
                targetAmount,
                currentAmount,
                new BigDecimal("100.00"),
                "USD",
                LocalDate.now().plusMonths(12),
                LocalDateTime.now()
        );
    }
}
