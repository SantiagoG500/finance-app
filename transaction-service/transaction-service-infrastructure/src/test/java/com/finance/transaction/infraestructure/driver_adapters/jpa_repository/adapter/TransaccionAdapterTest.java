package com.finance.transaction.infraestructure.driver_adapters.jpa_repository.adapter;

import com.finance.transaction.domain.model.Transaccion;
import com.finance.transaction.domain.model.enumns.FrecuenciaType;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.TransaccionEntity;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.repository.TransaccionJpaRepository;
import com.finance.transaction.infraestructure.entry_points.dto.TransaccionRequestDTO;
import com.finance.transaction.infraestructure.entry_points.dto.TransaccionResponseDTO;
import com.finance.transaction.infraestructure.mapper.TransaccionMapper;
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
class TransaccionAdapterTest {

    @Mock
    private TransaccionJpaRepository jpaRepository;

    @Mock
    private TransaccionMapper mapper;

    @InjectMocks
    private TransaccionAdapter transaccionAdapter;

    @Test
    void shouldSaveTransactionSuccessfully() {
        // Arrange
        UUID txId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Transaccion domain = buildDomain(txId, userId, catId);
        TransaccionEntity entity = buildEntity(txId, userId, catId);

        when(mapper.transaccionToEntity(domain)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.entityToTransaccion(entity)).thenReturn(domain);

        // Act
        Transaccion saved = transaccionAdapter.guardarTransaccion(domain);

        // Assert
        assertNotNull(saved);
        assertEquals(txId.toString(), saved.getId());
        verify(jpaRepository).save(entity);
    }

    @Test
    void shouldGetTransactionWhenFoundAndReturnNullWhenMissing() {
        // Arrange
        UUID txId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Transaccion domain = buildDomain(txId, userId, catId);
        TransaccionEntity entity = buildEntity(txId, userId, catId);

        when(jpaRepository.findByIdAndUserId(txId, userId))
                .thenReturn(Optional.of(entity))
                .thenReturn(Optional.empty());
        when(mapper.entityToTransaccion(entity)).thenReturn(domain);

        // Act
        Transaccion found = transaccionAdapter.obtenerTransaccion(txId.toString(), userId.toString());
        Transaccion missing = transaccionAdapter.obtenerTransaccion(txId.toString(), userId.toString());

        // Assert
        assertNotNull(found);
        assertNull(missing);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenGetTransactionReceivesInvalidUuid() {
        // Arrange
        String invalidUuid = "not-a-uuid";
        String userId = UUID.randomUUID().toString();

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transaccionAdapter.obtenerTransaccion(invalidUuid, userId)
        );

        // Assert
        assertNotNull(exception.getMessage());
    }

    @Test
    void shouldUpdateTransactionWhenExists() {
        // Arrange
        UUID txId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Transaccion domain = buildDomain(txId, userId, catId);
        TransaccionEntity existing = buildEntity(txId, userId, catId);

        when(jpaRepository.findByIdAndUserId(txId, userId)).thenReturn(Optional.of(existing));
        when(jpaRepository.save(existing)).thenReturn(existing);
        when(mapper.entityToTransaccion(existing)).thenReturn(domain);

        // Act
        Transaccion updated = transaccionAdapter.actualizarTransaccion(domain);

        // Assert
        assertNotNull(updated);
        assertEquals(domain.getAmount(), existing.getAmount());
        verify(jpaRepository).save(existing);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenUpdatingNonExistentTransaction() {
        // Arrange
        UUID txId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Transaccion domain = buildDomain(txId, userId, catId);
        when(jpaRepository.findByIdAndUserId(txId, userId)).thenReturn(Optional.empty());

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transaccionAdapter.actualizarTransaccion(domain)
        );

        // Assert
        assertEquals("Transacción no encontrada o no pertenece al usuario", exception.getMessage());
        verify(jpaRepository, never()).save(any());
    }

    @Test
    void shouldDeleteAndListTransactionsByUserSuccessfully() {
        // Arrange
        UUID txId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Transaccion domain = buildDomain(txId, userId, catId);
        TransaccionEntity entity = buildEntity(txId, userId, catId);

        when(jpaRepository.findByUserId(userId)).thenReturn(List.of(entity));
        when(mapper.entityToTransaccion(entity)).thenReturn(domain);

        // Act
        transaccionAdapter.eliminarTransaccion(txId.toString(), userId.toString());
        List<Transaccion> list = transaccionAdapter.obtenerTransaccionesPorUsuario(userId.toString());

        // Assert
        verify(jpaRepository).deleteByIdAndUserId(txId, userId);
        assertEquals(1, list.size());
        assertEquals(txId.toString(), list.get(0).getId());
    }

    @Test
    void shouldMapBetweenEntityDomainAndDtosUsingRealTransaccionMapper() {
        // Arrange
        TransaccionMapper realMapper = new TransaccionMapper();
        UUID txId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Transaccion domain = buildDomain(txId, userId, catId);
        TransaccionRequestDTO requestDto = new TransaccionRequestDTO(
                txId.toString(),
                catId.toString(),
                new BigDecimal("100.00"),
                "USD",
                TransaccionType.EXPENSE,
                FrecuenciaType.MONTHLY,
                "Desc",
                LocalDate.now()
        );

        // Act
        TransaccionEntity entity = realMapper.transaccionToEntity(domain);
        Transaccion backDomain = realMapper.entityToTransaccion(entity);
        Transaccion fromRequest = realMapper.requestDtoToTransaccion(requestDto, userId.toString());
        TransaccionResponseDTO responseDto = realMapper.transaccionToResponseDto(backDomain);

        // Assert
        assertNotNull(entity);
        assertEquals(txId, entity.getId());
        assertEquals(txId.toString(), backDomain.getId());
        assertEquals(userId.toString(), fromRequest.getExternalUserId());
        assertEquals(txId.toString(), responseDto.getId());
        assertNull(realMapper.entityToTransaccion(null));
        assertNull(realMapper.transaccionToEntity(null));
        assertNull(realMapper.requestDtoToTransaccion(null, userId.toString()));
        assertNull(realMapper.transaccionToResponseDto(null));
    }

    private Transaccion buildDomain(UUID id, UUID userId, UUID categoryId) {
        return new Transaccion(
                id.toString(),
                userId.toString(),
                categoryId.toString(),
                new BigDecimal("250.00"),
                "USD",
                TransaccionType.INCOME,
                FrecuenciaType.MONTHLY,
                "Salario",
                LocalDate.now(),
                LocalDateTime.now()
        );
    }

    private TransaccionEntity buildEntity(UUID id, UUID userId, UUID categoryId) {
        return new TransaccionEntity(
                id,
                userId,
                categoryId,
                new BigDecimal("250.00"),
                "USD",
                TransaccionType.INCOME,
                FrecuenciaType.MONTHLY,
                "Salario",
                LocalDate.now(),
                LocalDateTime.now()
        );
    }
}
