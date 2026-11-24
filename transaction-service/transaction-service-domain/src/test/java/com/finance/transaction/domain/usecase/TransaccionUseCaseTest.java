package com.finance.transaction.domain.usecase;

import com.finance.transaction.domain.model.Categoria;
import com.finance.transaction.domain.model.Transaccion;
import com.finance.transaction.domain.model.VerifiedUserSession;
import com.finance.transaction.domain.model.enumns.FrecuenciaType;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import com.finance.transaction.domain.model.gateway.AccountVerificationGateway;
import com.finance.transaction.domain.model.gateway.CategoriaGateway;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransaccionUseCaseTest {

    @Mock
    private TransaccionGateway transaccionGateway;

    @Mock
    private NotificationGateway notificationGateway;

    @Mock
    private AccountVerificationGateway accountVerificationGateway;

    @Mock
    private CategoriaGateway categoriaGateway;

    private TransaccionUseCase transaccionUseCase;

    @BeforeEach
    void setUp() {
        transaccionUseCase = new TransaccionUseCase(
                transaccionGateway,
                notificationGateway,
                accountVerificationGateway,
                categoriaGateway
        );
        lenient().when(accountVerificationGateway.verifyLoggedUser(anyString(), any()))
                .thenAnswer(inv -> new VerifiedUserSession(inv.getArgument(0), "Test User", "jwt-token"));
        lenient().when(categoriaGateway.obtenerCategoriaPorIdYUsuario(anyString(), anyString()))
                .thenAnswer(inv -> Optional.of(new Categoria(
                        inv.getArgument(0),
                        inv.getArgument(1),
                        "Salario",
                        TransaccionType.INCOME
                )));
    }

    @Test
    void shouldSaveTransactionVerifyLoggedUserValidateCategoryAndNotifyWhenValidWithEmail() {
        // Arrange
        Transaccion input = buildValidTransaction("Pago de nómina");
        input.setCreatedAt(null);
        when(transaccionGateway.guardarTransaccion(any(Transaccion.class))).thenReturn(input);

        // Act
        Transaccion saved = transaccionUseCase.crearTransaccion(input, "user@finance.com", "Bearer jwt-token");

        // Assert
        assertNotNull(saved);
        assertNotNull(saved.getCreatedAt());
        verify(accountVerificationGateway).verifyLoggedUser(input.getExternalUserId(), "Bearer jwt-token");
        verify(categoriaGateway).obtenerCategoriaPorIdYUsuario(input.getCategoryId(), input.getExternalUserId());
        verify(transaccionGateway).guardarTransaccion(input);
        verify(notificationGateway).notifyTransactionCreated(
                "user@finance.com",
                "INCOME",
                "1500.00 USD",
                "Pago de nómina"
        );
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenCategoryDoesNotExistOrBelongsToAnotherUser() {
        // Arrange
        Transaccion input = buildValidTransaction("Compra");
        when(categoriaGateway.obtenerCategoriaPorIdYUsuario(input.getCategoryId(), input.getExternalUserId()))
                .thenReturn(Optional.empty());

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transaccionUseCase.guardarTransaccion(input, "user@finance.com", "Bearer jwt-token")
        );

        // Assert
        assertEquals("La categoría especificada no existe o no pertenece al usuario", exception.getMessage());
        verify(transaccionGateway, never()).guardarTransaccion(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTransactionTypeDoesNotMatchCategoryType() {
        // Arrange
        Transaccion input = buildValidTransaction("Compra supermercado");
        input.setType(TransaccionType.EXPENSE);
        Categoria incomeCategory = new Categoria(
                input.getCategoryId(),
                input.getExternalUserId(),
                "Salario",
                TransaccionType.INCOME
        );
        when(categoriaGateway.obtenerCategoriaPorIdYUsuario(input.getCategoryId(), input.getExternalUserId()))
                .thenReturn(Optional.of(incomeCategory));

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transaccionUseCase.guardarTransaccion(input, "user@finance.com", "Bearer jwt-token")
        );

        // Assert
        assertEquals("El tipo de la transacción no coincide con el tipo de la categoría", exception.getMessage());
        verify(transaccionGateway, never()).guardarTransaccion(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenAccountVerificationReturnsNullOrBlankId() {
        // Arrange
        Transaccion input = buildValidTransaction("Pago");
        when(accountVerificationGateway.verifyLoggedUser(input.getExternalUserId(), "Bearer bad"))
                .thenReturn(null)
                .thenReturn(new VerifiedUserSession(" ", "User", "bad"));

        // Act
        IllegalStateException exNull = assertThrows(
                IllegalStateException.class,
                () -> transaccionUseCase.guardarTransaccion(input, "user@finance.com", "Bearer bad")
        );
        IllegalStateException exBlank = assertThrows(
                IllegalStateException.class,
                () -> transaccionUseCase.guardarTransaccion(input, "user@finance.com", "Bearer bad")
        );

        // Assert
        assertEquals("La sesión del usuario no pudo ser verificada", exNull.getMessage());
        assertEquals("La sesión del usuario no pudo ser verificada", exBlank.getMessage());
    }

    @Test
    void shouldSaveTransactionWithDefaultDescriptionWhenDescriptionIsBlank() {
        // Arrange
        Transaccion input = buildValidTransaction("   ");
        when(transaccionGateway.guardarTransaccion(input)).thenReturn(input);

        // Act
        Transaccion saved = transaccionUseCase.guardarTransaccion(input, "user@finance.com");

        // Assert
        assertNotNull(saved);
        verify(notificationGateway).notifyTransactionCreated(
                "user@finance.com",
                "INCOME",
                "1500.00 USD",
                "Sin descripción"
        );
    }

    @Test
    void shouldSaveTransactionUsingOverloadsAndConstructorsWithoutNotificationOrVerification() {
        // Arrange
        TransaccionUseCase useCaseSingleArg = new TransaccionUseCase(transaccionGateway);
        TransaccionUseCase useCaseTwoArgs = new TransaccionUseCase(transaccionGateway, notificationGateway);
        TransaccionUseCase useCaseThreeArgs = new TransaccionUseCase(
                transaccionGateway,
                notificationGateway,
                accountVerificationGateway
        );
        Transaccion input = buildValidTransaction("Alquiler");
        when(transaccionGateway.guardarTransaccion(input)).thenReturn(input);

        // Act
        Transaccion saved1 = transaccionUseCase.guardarTransaccion(input);
        Transaccion saved2 = useCaseSingleArg.guardarTransaccion(input, "user@finance.com");
        Transaccion saved3 = useCaseTwoArgs.guardarTransaccion(input);
        Transaccion saved4 = useCaseThreeArgs.guardarTransaccion(input);

        // Assert
        assertNotNull(saved1);
        assertNotNull(saved2);
        assertNotNull(saved3);
        assertNotNull(saved4);
        verifyNoInteractions(notificationGateway);
    }

    @Test
    void shouldCompleteSaveTransactionWhenNotificationGatewayThrowsException() {
        // Arrange
        Transaccion input = buildValidTransaction(null);
        when(transaccionGateway.guardarTransaccion(input)).thenReturn(input);
        doThrow(new RuntimeException("Notification error"))
                .when(notificationGateway)
                .notifyTransactionCreated(anyString(), anyString(), anyString(), anyString());

        // Act
        Transaccion saved = transaccionUseCase.guardarTransaccion(input, "user@finance.com");

        // Assert
        assertNotNull(saved);
        verify(notificationGateway).notifyTransactionCreated(
                "user@finance.com",
                "INCOME",
                "1500.00 USD",
                "Sin descripción"
        );
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenSavingNullTransaction() {
        // Arrange
        Transaccion nullTransaction = null;

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transaccionUseCase.guardarTransaccion(nullTransaction, "user@finance.com")
        );

        // Assert
        assertEquals("La transacción no puede ser nula.", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTransactionValidationFails() {
        // Arrange
        Transaccion noUser = buildValidTransaction("Desc");
        noUser.setExternalUserId(" ");
        Transaccion noCategory = buildValidTransaction("Desc");
        noCategory.setCategoryId(null);
        Transaccion zeroAmount = buildValidTransaction("Desc");
        zeroAmount.setAmount(BigDecimal.ZERO);
        Transaccion noCurrency = buildValidTransaction("Desc");
        noCurrency.setCurrency("");
        Transaccion noType = buildValidTransaction("Desc");
        noType.setType(null);
        Transaccion noFrequency = buildValidTransaction("Desc");
        noFrequency.setFrequency(null);
        Transaccion noDate = buildValidTransaction("Desc");
        noDate.setTransactionDate(null);

        // Act
        IllegalArgumentException exNull = assertThrows(
                IllegalArgumentException.class,
                () -> transaccionUseCase.validarTransaccionObj(null)
        );
        IllegalArgumentException exUser = assertThrows(
                IllegalArgumentException.class,
                () -> transaccionUseCase.guardarTransaccion(noUser)
        );
        IllegalArgumentException exCategory = assertThrows(
                IllegalArgumentException.class,
                () -> transaccionUseCase.guardarTransaccion(noCategory)
        );
        IllegalArgumentException exAmount = assertThrows(
                IllegalArgumentException.class,
                () -> transaccionUseCase.guardarTransaccion(zeroAmount)
        );
        IllegalArgumentException exCurrency = assertThrows(
                IllegalArgumentException.class,
                () -> transaccionUseCase.guardarTransaccion(noCurrency)
        );
        IllegalArgumentException exType = assertThrows(
                IllegalArgumentException.class,
                () -> transaccionUseCase.guardarTransaccion(noType)
        );
        IllegalArgumentException exFreq = assertThrows(
                IllegalArgumentException.class,
                () -> transaccionUseCase.guardarTransaccion(noFrequency)
        );
        IllegalArgumentException exDate = assertThrows(
                IllegalArgumentException.class,
                () -> transaccionUseCase.guardarTransaccion(noDate)
        );

        // Assert
        assertEquals("La transacción no puede ser nula.", exNull.getMessage());
        assertEquals("El usuario externo asociado no puede ser nulo o vacío.", exUser.getMessage());
        assertEquals("El ID de la categoría no puede ser nulo o vacío.", exCategory.getMessage());
        assertEquals("El monto de la transacción debe ser mayor a cero.", exAmount.getMessage());
        assertEquals("La moneda no puede ser nula o vacía.", exCurrency.getMessage());
        assertEquals("El tipo de transacción no puede ser nulo.", exType.getMessage());
        assertEquals("La frecuencia no puede ser nula.", exFreq.getMessage());
        assertEquals("La fecha de transacción no puede ser nula.", exDate.getMessage());
    }

    @Test
    void shouldGetTransactionSuccessfullyWhenParametersAreValid() {
        // Arrange
        Transaccion expected = buildValidTransaction("Compra");
        when(transaccionGateway.obtenerTransaccion(expected.getId(), expected.getExternalUserId()))
                .thenReturn(expected);

        // Act
        Transaccion actual = transaccionUseCase.obtenerTransaccion(expected.getId(), expected.getExternalUserId());

        // Assert
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenGetTransactionParamsAreInvalid() {
        // Arrange
        String validId = UUID.randomUUID().toString();

        // Act
        IllegalArgumentException exId = assertThrows(
                IllegalArgumentException.class,
                () -> transaccionUseCase.obtenerTransaccion(" ", validId)
        );
        IllegalArgumentException exUser = assertThrows(
                IllegalArgumentException.class,
                () -> transaccionUseCase.obtenerTransaccion(validId, null)
        );

        // Assert
        assertEquals("El ID de la transacción no puede ser nulo o vacío.", exId.getMessage());
        assertEquals("El ID externo del usuario no puede ser nulo o vacío.", exUser.getMessage());
    }

    @Test
    void shouldFilterTransactionsByTypeSuccessfully() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        Transaccion income = buildValidTransaction("Salario");
        income.setType(TransaccionType.INCOME);
        Transaccion expense = buildValidTransaction("Mercado");
        expense.setType(TransaccionType.EXPENSE);

        when(transaccionGateway.obtenerTransaccionesPorUsuario(userId))
                .thenReturn(Arrays.asList(income, expense, null));

        // Act
        List<Transaccion> incomes = transaccionUseCase.obtenerTransaccionesPorTipo(userId, TransaccionType.INCOME);
        List<Transaccion> expenses = transaccionUseCase.obtenerTransaccionesPorTipo(userId, TransaccionType.EXPENSE);

        // Assert
        assertEquals(1, incomes.size());
        assertEquals(TransaccionType.INCOME, incomes.get(0).getType());
        assertEquals(1, expenses.size());
        assertEquals(TransaccionType.EXPENSE, expenses.get(0).getType());
    }

    @Test
    void shouldReturnEmptyListWhenNoTransactionsExistForTypeFilter() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        when(transaccionGateway.obtenerTransaccionesPorUsuario(userId))
                .thenReturn(Collections.emptyList())
                .thenReturn(null);

        // Act
        List<Transaccion> resultEmpty = transaccionUseCase.obtenerTransaccionesPorTipo(userId, TransaccionType.INCOME);
        List<Transaccion> resultNull = transaccionUseCase.obtenerTransaccionesPorTipo(userId, TransaccionType.EXPENSE);

        // Assert
        assertTrue(resultEmpty.isEmpty());
        assertTrue(resultNull.isEmpty());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFilterByTypeParamsAreInvalid() {
        // Arrange
        String userId = UUID.randomUUID().toString();

        // Act
        IllegalArgumentException exUser = assertThrows(
                IllegalArgumentException.class,
                () -> transaccionUseCase.obtenerTransaccionesPorTipo("  ", TransaccionType.INCOME)
        );
        IllegalArgumentException exType = assertThrows(
                IllegalArgumentException.class,
                () -> transaccionUseCase.obtenerTransaccionesPorTipo(userId, null)
        );

        // Assert
        assertEquals("El ID externo del usuario no puede ser nulo o vacío.", exUser.getMessage());
        assertEquals("El tipo de transacción no puede ser nulo.", exType.getMessage());
    }

    private Transaccion buildValidTransaction(String description) {
        return new Transaccion(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                new BigDecimal("1500.00"),
                "USD",
                TransaccionType.INCOME,
                FrecuenciaType.MONTHLY,
                description,
                LocalDate.now(),
                LocalDateTime.now()
        );
    }
}
