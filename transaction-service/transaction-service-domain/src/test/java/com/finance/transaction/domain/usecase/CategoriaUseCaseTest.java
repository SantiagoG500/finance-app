package com.finance.transaction.domain.usecase;

import com.finance.transaction.domain.model.Categoria;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import com.finance.transaction.domain.model.gateway.CategoriaGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaUseCaseTest {

    @Mock
    private CategoriaGateway categoriaGateway;

    @InjectMocks
    private CategoriaUseCase categoriaUseCase;

    @Test
    void shouldSaveCategorySuccessfullyWhenValid() {
        // Arrange
        Categoria category = buildValidCategory();
        when(categoriaGateway.guardarCategoria(category)).thenReturn(category);

        // Act
        Categoria saved = categoriaUseCase.guardarCategoria(category);

        // Assert
        assertNotNull(saved);
        assertEquals("Alimentación", saved.getName());
        verify(categoriaGateway).guardarCategoria(category);
    }

    @Test
    void shouldGetCategorySuccessfullyWhenParamsAreValid() {
        // Arrange
        Categoria category = buildValidCategory();
        when(categoriaGateway.obtenerCategoria(category.getId(), category.getExternalUserId()))
                .thenReturn(category);

        // Act
        Categoria found = categoriaUseCase.obtenerCategoria(category.getId(), category.getExternalUserId());

        // Assert
        assertNotNull(found);
        assertEquals(category.getId(), found.getId());
    }

    @Test
    void shouldUpdateAndDeleteCategorySuccessfullyWhenValid() {
        // Arrange
        Categoria category = buildValidCategory();
        when(categoriaGateway.actualizarCategoria(category)).thenReturn(category);

        // Act
        Categoria updated = categoriaUseCase.actualizarCategoria(category);
        categoriaUseCase.eliminarCategoria(category.getId(), category.getExternalUserId());

        // Assert
        assertNotNull(updated);
        verify(categoriaGateway).actualizarCategoria(category);
        verify(categoriaGateway).eliminarCategoria(category.getId(), category.getExternalUserId());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenCategoryValidationFails() {
        // Arrange
        Categoria noUser = buildValidCategory();
        noUser.setExternalUserId(" ");
        Categoria noName = buildValidCategory();
        noName.setName(null);
        Categoria noType = buildValidCategory();
        noType.setType(null);
        String validId = UUID.randomUUID().toString();

        // Act
        IllegalArgumentException exNull = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaUseCase.guardarCategoria(null)
        );
        IllegalArgumentException exUser = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaUseCase.guardarCategoria(noUser)
        );
        IllegalArgumentException exName = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaUseCase.guardarCategoria(noName)
        );
        IllegalArgumentException exType = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaUseCase.guardarCategoria(noType)
        );
        IllegalArgumentException exBadId = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaUseCase.obtenerCategoria("", validId)
        );
        IllegalArgumentException exBadExternal = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaUseCase.obtenerCategoria(validId, "  ")
        );

        // Assert
        assertEquals("La categoría no puede ser nula.", exNull.getMessage());
        assertEquals("El usuario externo asociado no puede ser nulo o vacío.", exUser.getMessage());
        assertEquals("El nombre de la categoría no puede ser nulo o vacío.", exName.getMessage());
        assertEquals("El tipo de categoría no puede ser nulo.", exType.getMessage());
        assertEquals("El ID de la categoría no puede ser nulo o vacío.", exBadId.getMessage());
        assertEquals("El ID externo del usuario no puede ser nulo o vacío.", exBadExternal.getMessage());
    }

    private Categoria buildValidCategory() {
        return new Categoria(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "Alimentación",
                TransaccionType.EXPENSE,
                "food-icon"
        );
    }
}
