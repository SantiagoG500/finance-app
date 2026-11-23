package com.finance.transaction.infraestructure.driver_adapters.jpa_repository.adapter;

import com.finance.transaction.domain.model.MetaAhorro;
import com.finance.transaction.domain.model.ResumenFinanciero;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.MetaAhorrosEntity;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.repository.MetaAhorrosJpaRepository;
import com.finance.transaction.infraestructure.entry_points.dto.MetaAhorroRequestDTO;
import com.finance.transaction.infraestructure.entry_points.dto.MetaAhorroResponseDTO;
import com.finance.transaction.infraestructure.entry_points.dto.ResumenFinancieroDTO;
import com.finance.transaction.infraestructure.mapper.MetaAhorrosMapper;
import com.finance.transaction.infraestructure.mapper.ResumenFinancieroMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    void shouldThrowIllegalArgumentExceptionWhenUpdatingNonExistentSavingsGoal() {
        // Arrange
        UUID goalId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MetaAhorro domain = buildDomain(goalId, userId);
        when(jpaRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.empty());

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> metaAhorrosAdapter.actualizarMetaAhorros(domain)
        );

        // Assert
        assertEquals("Meta de ahorro no encontrada o no pertenece al usuario", exception.getMessage());
        verify(jpaRepository, never()).save(any());
    }

    @Test
    void shouldDeleteAndListSavingsGoalsByUserSuccessfully() {
        // Arrange
        UUID goalId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MetaAhorro domain = buildDomain(goalId, userId);
        MetaAhorrosEntity entity = buildEntity(goalId, userId);

        when(jpaRepository.findByUserId(userId)).thenReturn(List.of(entity));
        when(mapper.toMetaAhorro(entity)).thenReturn(domain);

        // Act
        metaAhorrosAdapter.eliminarMetaAhorros(goalId.toString(), userId.toString());
        List<MetaAhorro> list = metaAhorrosAdapter.obtenerMetasPorUsuario(userId.toString());

        // Assert
        verify(jpaRepository).deleteByIdAndUserId(goalId, userId);
        assertEquals(1, list.size());
        assertEquals(goalId.toString(), list.get(0).getId());
    }

    @Test
    void shouldMapBetweenEntityDomainAndDtosUsingRealMappers() {
        // Arrange
        MetaAhorrosMapper realGoalMapper = new MetaAhorrosMapper();
        ResumenFinancieroMapper realSummaryMapper = new ResumenFinancieroMapper(realGoalMapper);
        UUID goalId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MetaAhorro domain = buildDomain(goalId, userId);
        MetaAhorroRequestDTO requestDto = new MetaAhorroRequestDTO(
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
        MetaAhorro fromRequest = realGoalMapper.requestDtoToMetaAhorro(requestDto, userId.toString());
        MetaAhorroResponseDTO responseDto = realGoalMapper.metaAhorroToResponseDto(backDomain);
        ResumenFinancieroDTO summaryDto = realSummaryMapper.resumenFinancieroToDto(resumen);

        // Assert
        assertNotNull(entity);
        assertEquals(goalId.toString(), backDomain.getId());
        assertEquals("Casa", fromRequest.getTitle());
        assertEquals(goalId.toString(), responseDto.getId());
        assertEquals(1, summaryDto.getMetasAhorro().size());
        assertNull(realGoalMapper.entityToMetaAhorro(null));
        assertNull(realGoalMapper.metaAhorroToEntity(null));
        assertNull(realGoalMapper.requestDtoToMetaAhorro(null, userId.toString()));
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
