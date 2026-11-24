package com.finance.transaction.domain.usecase;

import com.finance.transaction.domain.model.Categoria;
import com.finance.transaction.domain.model.VerifiedUserSession;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import com.finance.transaction.domain.model.gateway.AccountVerificationGateway;
import com.finance.transaction.domain.model.gateway.CategoriaGateway;
import com.finance.transaction.domain.model.gateway.TransaccionGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaUseCaseTest {

    @Mock
    private CategoriaGateway categoriaGateway;

    @Mock
    private TransaccionGateway transaccionGateway;

    @Mock
    private AccountVerificationGateway accountVerificationGateway;

    private CategoriaUseCase categoriaUseCase;

    @BeforeEach
    void setUp() {
        categoriaUseCase = new CategoriaUseCase(
                categoriaGateway,
                transaccionGateway,
                accountVerificationGateway
        );
        lenient().when(accountVerificationGateway.verifyLoggedUser(anyString(), any()))
                .thenAnswer(inv -> new VerifiedUserSession(inv.getArgument(0), "Test User", "jwt-token"));
    }

    @Test
    void shouldCreateCategoryWhenInputIsValidAndNotDuplicated() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        String categoryId = UUID.randomUUID().toString();
        Categoria input = new Categoria(null, "  " + userId + "  ", "  Salario  ", TransaccionType.INCOME);
        Categoria saved = new Categoria(categoryId, userId, "Salario", TransaccionType.INCOME);

        when(categoriaGateway.existeCategoriaConNombreYTipo(userId, "Salario", TransaccionType.INCOME)).thenReturn(false);
        when(categoriaGateway.guardarCategoria(input)).thenReturn(saved);

        // Act
        Categoria result = categoriaUseCase.crearCategoria(input, "Bearer jwt-token");

        // Assert
        assertNotNull(result);
        assertEquals(categoryId, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals("Salario", result.getName());
        assertEquals(TransaccionType.INCOME, result.getType());
        verify(accountVerificationGateway).verifyLoggedUser(userId, "Bearer jwt-token");
        verify(categoriaGateway).guardarCategoria(input);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenCreateCategoryFieldsAreInvalid() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        Categoria noUser = new Categoria(null, "  ", "Salario", TransaccionType.INCOME);
        Categoria noName = new Categoria(null, userId, "   ", TransaccionType.INCOME);
        Categoria noType = new Categoria(null, userId, "Salario", null);

        // Act
        IllegalArgumentException exNull = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaUseCase.crearCategoria(null, "Bearer jwt-token")
        );
        IllegalArgumentException exUser = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaUseCase.crearCategoria(noUser, "Bearer jwt-token")
        );
        IllegalArgumentException exName = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaUseCase.crearCategoria(noName, "Bearer jwt-token")
        );
        IllegalArgumentException exType = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaUseCase.crearCategoria(noType, "Bearer jwt-token")
        );

        // Assert
        assertEquals("La categoría no puede ser nula.", exNull.getMessage());
        assertEquals("El ID del usuario no puede ser nulo o vacío.", exUser.getMessage());
        assertEquals("El nombre de la categoría no puede ser nulo o vacío.", exName.getMessage());
        assertEquals("El tipo de la categoría no puede ser nulo.", exType.getMessage());
        verify(categoriaGateway, never()).guardarCategoria(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenCategoryAlreadyExistsForUser() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        Categoria input = new Categoria(null, userId, "  Salario ", TransaccionType.INCOME);
        when(categoriaGateway.existeCategoriaConNombreYTipo(userId, "Salario", TransaccionType.INCOME)).thenReturn(true);

        // Act
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> categoriaUseCase.crearCategoria(input, "Bearer jwt-token")
        );

        // Assert
        assertEquals("Ya existe una categoría con ese nombre y tipo para el usuario", exception.getMessage());
        verify(categoriaGateway, never()).guardarCategoria(any());
    }

    @Test
    void shouldListCategoriesByUserAndHandleNullResponseFromGateway() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        Categoria categoria = new Categoria(UUID.randomUUID().toString(), userId, "Alimentación", TransaccionType.EXPENSE);
        when(categoriaGateway.obtenerCategoriasPorUsuario(userId))
                .thenReturn(List.of(categoria))
                .thenReturn(null);

        // Act
        List<Categoria> resultList = categoriaUseCase.listarCategoriasPorUsuario(userId, "Bearer jwt-token");
        List<Categoria> emptyList = categoriaUseCase.listarCategoriasPorUsuario(userId, "Bearer jwt-token");

        // Assert
        assertEquals(1, resultList.size());
        assertEquals("Alimentación", resultList.get(0).getName());
        assertTrue(emptyList.isEmpty());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenListCategoriesUserIdIsInvalid() {
        // Arrange
        String blankUserId = "   ";

        // Act
        IllegalArgumentException exBlank = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaUseCase.listarCategoriasPorUsuario(blankUserId, "Bearer jwt-token")
        );
        IllegalArgumentException exNull = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaUseCase.listarCategoriasPorUsuario(null, "Bearer jwt-token")
        );

        // Assert
        assertEquals("El ID del usuario no puede ser nulo o vacío.", exBlank.getMessage());
        assertEquals("El ID del usuario no puede ser nulo o vacío.", exNull.getMessage());
    }

    @Test
    void shouldDeleteCategorySuccessfullyWhenOwnedAndNotUsedInTransactions() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        String categoryId = UUID.randomUUID().toString();
        Categoria existing = new Categoria(categoryId, userId, "Ocio", TransaccionType.EXPENSE);

        when(categoriaGateway.obtenerCategoriaPorIdYUsuario(categoryId, userId)).thenReturn(Optional.of(existing));
        when(transaccionGateway.existeTransaccionPorCategoriaYUsuario(categoryId, userId)).thenReturn(false);

        // Act
        categoriaUseCase.eliminarCategoria(categoryId, userId, "Bearer jwt-token");

        // Assert
        verify(accountVerificationGateway).verifyLoggedUser(userId, "Bearer jwt-token");
        verify(categoriaGateway).eliminarCategoria(categoryId, userId);
    }

    @Test
    void shouldThrowNoSuchElementExceptionWhenDeletingNonExistentOrOtherUsersCategory() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        String categoryId = UUID.randomUUID().toString();
        when(categoriaGateway.obtenerCategoriaPorIdYUsuario(categoryId, userId)).thenReturn(Optional.empty());

        // Act
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> categoriaUseCase.eliminarCategoria(categoryId, userId, "Bearer jwt-token")
        );

        // Assert
        assertEquals("Categoría no encontrada", exception.getMessage());
        verify(categoriaGateway, never()).eliminarCategoria(anyString(), anyString());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenDeletingCategoryWithAssociatedTransactions() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        String categoryId = UUID.randomUUID().toString();
        Categoria existing = new Categoria(categoryId, userId, "Salario", TransaccionType.INCOME);

        when(categoriaGateway.obtenerCategoriaPorIdYUsuario(categoryId, userId)).thenReturn(Optional.of(existing));
        when(transaccionGateway.existeTransaccionPorCategoriaYUsuario(categoryId, userId)).thenReturn(true);

        // Act
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> categoriaUseCase.eliminarCategoria(categoryId, userId, "Bearer jwt-token")
        );

        // Assert
        assertEquals("No se puede eliminar la categoría porque tiene transacciones asociadas", exception.getMessage());
        verify(categoriaGateway, never()).eliminarCategoria(anyString(), anyString());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOrIllegalStateExceptionOnDeleteValidationAndSessionVerification() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        String categoryId = UUID.randomUUID().toString();
        when(accountVerificationGateway.verifyLoggedUser(userId, "Bearer bad"))
                .thenReturn(null)
                .thenReturn(new VerifiedUserSession(" ", "User", "bad"));

        // Act
        IllegalArgumentException exId = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaUseCase.eliminarCategoria(" ", userId, "Bearer jwt-token")
        );
        IllegalArgumentException exUser = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaUseCase.eliminarCategoria(categoryId, " ", "Bearer jwt-token")
        );
        IllegalStateException exNullSession = assertThrows(
                IllegalStateException.class,
                () -> categoriaUseCase.eliminarCategoria(categoryId, userId, "Bearer bad")
        );
        IllegalStateException exBlankSession = assertThrows(
                IllegalStateException.class,
                () -> categoriaUseCase.eliminarCategoria(categoryId, userId, "Bearer bad")
        );

        // Assert
        assertEquals("El ID de la categoría no puede ser nulo o vacío.", exId.getMessage());
        assertEquals("El ID del usuario no puede ser nulo o vacío.", exUser.getMessage());
        assertEquals("La sesión del usuario no pudo ser verificada", exNullSession.getMessage());
        assertEquals("La sesión del usuario no pudo ser verificada", exBlankSession.getMessage());
    }
}
