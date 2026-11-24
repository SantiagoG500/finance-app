package com.finance.transaction.infraestructure.driver_adapters.jpa_repository.adapter;

import com.finance.transaction.domain.model.MetaAhorro;
import com.finance.transaction.domain.model.PaginatedMetaAhorro;
import com.finance.transaction.domain.model.ResumenFinanciero;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.MetaAhorrosEntity;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.repository.MetaAhorrosJpaRepository;
import com.finance.transaction.infraestructure.entry_points.dto.CreateMetaAhorroRequestDTO;
import com.finance.transaction.infraestructure.entry_points.dto.MetaAhorroRequestDTO;
import com.finance.transaction.infraestructure.entry_points.dto.MetaAhorroResponseDTO;
import com.finance.transaction.infraestructure.entry_points.dto.ResumenFinancieroDTO;
import com.finance.transaction.infraestructure.entry_points.dto.UpdateMetaAhorroRequestDTO;
import com.finance.transaction.infraestructure.mapper.MetaAhorrosMapper;
import com.finance.transaction.infraestructure.mapper.ResumenFinancieroMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetaAhorrosAdapterTest {

    @Mock
    private MetaAhorrosJpaRepository jpaRepository;

    @Mock
    private MetaAhorrosMapper mapper;

    @InjectMocks
    private MetaAhorrosAdapter metaAhorrosAdapter;

    @Test
    void shouldSaveSavingsGoalSuccessfully() {
        // Arrange
        UUID goalId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MetaAhorro domain = buildDomain(goalId, userId);
        MetaAhorrosEntity entity = buildEntity(goalId, userId);

        when(mapper.toMetaAhorrosEntity(domain)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toMetaAhorro(entity)).thenReturn(domain);

        // Act
        MetaAhorro saved = metaAhorrosAdapter.guardarMetaAhorros(domain);

        // Assert
        assertNotNull(saved);
        assertEquals(goalId.toString(), saved.getId());
        verify(jpaRepository).save(entity);
    }

    @Test
    void shouldGetSavingsGoalWhenFoundAndReturnNullWhenMissing() {
        // Arrange
        UUID goalId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MetaAhorro domain = buildDomain(goalId, userId);
        MetaAhorrosEntity entity = buildEntity(goalId, userId);

        when(jpaRepository.findByIdAndUserId(goalId, userId))
                .thenReturn(Optional.of(entity))
                .thenReturn(Optional.empty());
        when(mapper.toMetaAhorro(entity)).thenReturn(domain);

        // Act
        MetaAhorro found = metaAhorrosAdapter.obtenerMetaAhorros(goalId.toString(), userId.toString());
        MetaAhorro missing = metaAhorrosAdapter.obtenerMetaAhorros(goalId.toString(), userId.toString());

        // Assert
        assertNotNull(found);
        assertNull(missing);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenGetSavingsGoalReceivesInvalidUuid() {
        // Arrange
        String invalidUuid = "invalid-uuid";
        String userId = UUID.randomUUID().toString();

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosAdapter.obtenerMetaAhorros(invalidUuid, userId)
        );

        // Assert
        assertNotNull(exception.getMessage());
    }

    @Test
    void shouldUpdateSavingsGoalWhenExists() {
        // Arrange
        UUID goalId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MetaAhorro domain = buildDomain(goalId, userId);
        MetaAhorrosEntity existing = buildEntity(goalId, userId);

        when(jpaRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(existing));
        when(jpaRepository.save(existing)).thenReturn(existing);
        when(mapper.toMetaAhorro(existing)).thenReturn(domain);

        // Act
        MetaAhorro updated = metaAhorrosAdapter.actualizarMetaAhorros(domain);

        // Assert
        assertNotNull(updated);
        assertEquals(domain.getTitle(), existing.getTitle());
        verify(jpaRepository).save(existing);
    }

    @Test
    void shouldThrowNoSuchElementExceptionWhenUpdatingOrDeletingNonExistentSavingsGoal() {
        // Arrange
        UUID goalId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MetaAhorro domain = buildDomain(goalId, userId);
        when(jpaRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.empty());

        // Act
        NoSuchElementException exUpdate = assertThrows(
                NoSuchElementException.class,
                () -> metaAhorrosAdapter.actualizarMetaAhorros(domain)
        );
        NoSuchElementException exDelete = assertThrows(
                NoSuchElementException.class,
                () -> metaAhorrosAdapter.eliminarMetaAhorros(goalId.toString(), userId.toString())
        );

        // Assert
        assertEquals("Meta de ahorro no encontrada o no pertenece al usuario", exUpdate.getMessage());
        assertEquals("Meta de ahorro no encontrada o no pertenece al usuario", exDelete.getMessage());
        verify(jpaRepository, never()).save(any());
        verify(jpaRepository, never()).deleteByIdAndUserId(any(), any());
    }

    @Test
    void shouldDeleteListAndPaginateSavingsGoalsByUserSuccessfully() {
        // Arrange
        UUID goalId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MetaAhorro domain = buildDomain(goalId, userId);
        MetaAhorrosEntity entity = buildEntity(goalId, userId);
        PageRequest expectedPageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MetaAhorrosEntity> entityPage = new PageImpl<>(List.of(entity), expectedPageRequest, 1L);

        when(jpaRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(entity));
        when(jpaRepository.findByUserId(userId)).thenReturn(List.of(entity));
        when(jpaRepository.findByUserId(eq(userId), any(Pageable.class))).thenReturn(entityPage);
        when(mapper.toMetaAhorro(entity)).thenReturn(domain);

        // Act
        metaAhorrosAdapter.eliminarMetaAhorros(goalId.toString(), userId.toString());
        List<MetaAhorro> list = metaAhorrosAdapter.obtenerMetasPorUsuario(userId.toString());
        PaginatedMetaAhorro paginated = metaAhorrosAdapter.obtenerMetasPaginadasPorUsuario(userId.toString(), 0, 10);

        // Assert
        verify(jpaRepository).deleteByIdAndUserId(goalId, userId);
        assertEquals(1, list.size());
        assertEquals(goalId.toString(), list.get(0).getId());
        assertEquals(1, paginated.content().size());
        assertEquals(0, paginated.page());
        assertEquals(10, paginated.size());
        assertEquals(1L, paginated.totalElements());
        assertEquals(1, paginated.totalPages());
        assertTrue(paginated.last());
    }

    @Test
    void shouldMapBetweenEntityDomainAndDtosUsingRealMappers() {
        // Arrange
        MetaAhorrosMapper realGoalMapper = new MetaAhorrosMapper();
        ResumenFinancieroMapper realSummaryMapper = new ResumenFinancieroMapper(realGoalMapper);
        UUID goalId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MetaAhorro domain = buildDomain(goalId, userId);
        CreateMetaAhorroRequestDTO createDto = new CreateMetaAhorroRequestDTO(
                "Casa",
                new BigDecimal("10000.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                "usd",
                LocalDate.now().plusYears(2)
        );
        UpdateMetaAhorroRequestDTO updateDto = new UpdateMetaAhorroRequestDTO(
                "Casa Nueva",
                new BigDecimal("12000.00"),
                new BigDecimal("2000.00"),
                new BigDecimal("600.00"),
                "eur",
                LocalDate.now().plusYears(2)
        );
        MetaAhorroRequestDTO legacyDto = new MetaAhorroRequestDTO(
                "Casa",
                new BigDecimal("10000.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                "USD",
                LocalDate.now().plusYears(2)
        );
        ResumenFinanciero resumen = new ResumenFinanciero();
        resumen.setUsuarioId(userId.toString());
        resumen.setMoneda("USD");
        resumen.setMetasAhorro(List.of(domain));

        // Act
        MetaAhorrosEntity entity = realGoalMapper.toMetaAhorrosEntity(domain);
        MetaAhorro backDomain = realGoalMapper.toMetaAhorro(entity);
        MetaAhorro fromCreate = realGoalMapper.requestDtoToMetaAhorro(createDto, userId.toString());
        MetaAhorro fromUpdate = realGoalMapper.requestDtoToMetaAhorro(goalId.toString(), updateDto, userId.toString());
        MetaAhorro fromLegacy = realGoalMapper.requestDtoToMetaAhorro(legacyDto, userId.toString());
        MetaAhorroResponseDTO responseDto = realGoalMapper.metaAhorroToResponseDto(backDomain);
        ResumenFinancieroDTO summaryDto = realSummaryMapper.resumenFinancieroToDto(resumen);

        // Assert
        assertNotNull(entity);
        assertEquals(goalId.toString(), backDomain.getId());
        assertEquals("Casa", fromCreate.getTitle());
        assertEquals("USD", fromCreate.getCurrency());
        assertEquals("EUR", fromUpdate.getCurrency());
        assertEquals("Casa", fromLegacy.getTitle());
        assertEquals(goalId.toString(), responseDto.getId());
        assertEquals(1, summaryDto.getMetasAhorro().size());
        assertNull(realGoalMapper.entityToMetaAhorro(null));
        assertNull(realGoalMapper.metaAhorroToEntity(null));
        assertNull(realGoalMapper.requestDtoToMetaAhorro((CreateMetaAhorroRequestDTO) null, userId.toString()));
        assertNull(realGoalMapper.requestDtoToMetaAhorro(goalId.toString(), (UpdateMetaAhorroRequestDTO) null, userId.toString()));
        assertNull(realGoalMapper.requestDtoToMetaAhorro((MetaAhorroRequestDTO) null, userId.toString()));
        assertNull(realGoalMapper.metaAhorroToResponseDto(null));
        assertNull(realSummaryMapper.resumenFinancieroToDto(null));
    }

    private MetaAhorro buildDomain(UUID id, UUID userId) {
        return new MetaAhorro(
                id.toString(),
                userId.toString(),
                "Viaje",
                new BigDecimal("3000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("250.00"),
                "USD",
                LocalDate.now().plusMonths(10),
                LocalDateTime.now()
        );
    }

    private MetaAhorrosEntity buildEntity(UUID id, UUID userId) {
        return new MetaAhorrosEntity(
                id,
                userId,
                "Viaje",
                new BigDecimal("3000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("250.00"),
                LocalDate.now().plusMonths(10),
                "USD",
                LocalDateTime.now()
        );
    }
}
