package com.finance.transaction.infraestructure.driver_adapters.jpa_repository.adapter;

import com.finance.transaction.domain.model.Categoria;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.CategoriaEntity;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.repository.CategoriaJpaRepository;
import com.finance.transaction.infraestructure.entry_points.dto.CategoriaResponseDTO;
import com.finance.transaction.infraestructure.entry_points.dto.CreateCategoriaRequestDTO;
import com.finance.transaction.infraestructure.mapper.CategoriaMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaAdapterTest {

    @Mock
    private CategoriaJpaRepository jpaRepository;

    @Mock
    private CategoriaMapper mapper;

    @InjectMocks
    private CategoriaAdapter categoriaAdapter;

    @Test
    void shouldSaveCategorySuccessfully() {
        // Arrange
        UUID catId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Categoria domain = new Categoria(catId.toString(), userId.toString(), "Salario", TransaccionType.INCOME);
        CategoriaEntity entity = new CategoriaEntity(catId, userId, "Salario", TransaccionType.INCOME);

        when(mapper.categoriaToEntity(domain)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.entityToCategoria(entity)).thenReturn(domain);

        // Act
        Categoria saved = categoriaAdapter.guardarCategoria(domain);

        // Assert
        assertNotNull(saved);
        assertEquals(catId.toString(), saved.getId());
        assertEquals(userId.toString(), saved.getUserId());
        assertEquals("Salario", saved.getName());
        assertEquals(TransaccionType.INCOME, saved.getType());
        verify(jpaRepository).save(entity);
    }

    @Test
    void shouldListCategoriesByUserAndFindCategoryByIdAndUser() {
        // Arrange
        UUID catId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Categoria domain = new Categoria(catId.toString(), userId.toString(), "Salario", TransaccionType.INCOME);
        CategoriaEntity entity = new CategoriaEntity(catId, userId, "Salario", TransaccionType.INCOME);

        when(jpaRepository.findByUserId(userId)).thenReturn(List.of(entity));
        when(jpaRepository.findByIdAndUserId(catId, userId))
                .thenReturn(Optional.of(entity))
                .thenReturn(Optional.empty());
        when(mapper.entityToCategoria(entity)).thenReturn(domain);

        // Act
        List<Categoria> list = categoriaAdapter.obtenerCategoriasPorUsuario(userId.toString());
        Optional<Categoria> found = categoriaAdapter.obtenerCategoriaPorIdYUsuario(catId.toString(), userId.toString());
        Optional<Categoria> missing = categoriaAdapter.obtenerCategoriaPorIdYUsuario(catId.toString(), userId.toString());

        // Assert
        assertEquals(1, list.size());
        assertEquals(catId.toString(), list.get(0).getId());
        assertTrue(found.isPresent());
        assertEquals("Salario", found.get().getName());
        assertFalse(missing.isPresent());
    }

    @Test
    void shouldCheckExistenceAndDeleteCategoryByIdAndUser() {
        // Arrange
        UUID catId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(jpaRepository.existsByUserIdAndNameIgnoreCaseAndType(userId, "Salario", TransaccionType.INCOME))
                .thenReturn(true);

        // Act
        boolean exists = categoriaAdapter.existeCategoriaConNombreYTipo(userId.toString(), "Salario", TransaccionType.INCOME);
        categoriaAdapter.eliminarCategoria(catId.toString(), userId.toString());

        // Assert
        assertTrue(exists);
        verify(jpaRepository).existsByUserIdAndNameIgnoreCaseAndType(userId, "Salario", TransaccionType.INCOME);
        verify(jpaRepository).deleteByIdAndUserId(catId, userId);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenUuidIsInvalid() {
        // Arrange
        String invalidUuid = "invalid-uuid";
        String validUuid = UUID.randomUUID().toString();

        // Act
        IllegalArgumentException exList = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaAdapter.obtenerCategoriasPorUsuario(invalidUuid)
        );
        IllegalArgumentException exDelete = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaAdapter.eliminarCategoria(invalidUuid, validUuid)
        );

        // Assert
        assertNotNull(exList.getMessage());
        assertNotNull(exDelete.getMessage());
    }

    @Test
    void shouldMapBetweenEntityDomainAndDtosUsingRealCategoriaMapper() {
        // Arrange
        CategoriaMapper realMapper = new CategoriaMapper();
        UUID catId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Categoria domain = new Categoria(catId.toString(), userId.toString(), "Salario", TransaccionType.INCOME);
        CreateCategoriaRequestDTO requestDto = new CreateCategoriaRequestDTO("  Salario  ", TransaccionType.INCOME);

        // Act
        CategoriaEntity entity = realMapper.categoriaToEntity(domain);
        Categoria backDomain = realMapper.entityToCategoria(entity);
        Categoria fromRequest = realMapper.requestDtoToCategoria(requestDto, userId.toString());
        CategoriaResponseDTO responseDto = realMapper.categoriaToResponseDto(backDomain);

        // Assert
        assertNotNull(entity);
        assertEquals(catId, entity.getId());
        assertEquals(userId, entity.getUserId());
        assertEquals("Salario", backDomain.getName());
        assertEquals("Salario", fromRequest.getName());
        assertEquals(userId.toString(), fromRequest.getUserId());
        assertEquals(catId.toString(), responseDto.getId());
        assertEquals(userId.toString(), responseDto.getUserId());
        assertEquals(TransaccionType.INCOME, responseDto.getType());
        assertNull(realMapper.entityToCategoria(null));
        assertNull(realMapper.categoriaToEntity(null));
        assertNull(realMapper.requestDtoToCategoria(null, userId.toString()));
        assertNull(realMapper.categoriaToResponseDto(null));
    }
}
