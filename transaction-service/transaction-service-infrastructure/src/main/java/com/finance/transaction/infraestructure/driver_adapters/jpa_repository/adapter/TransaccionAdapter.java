package com.finance.transaction.infraestructure.driver_adapters.jpa_repository.adapter;

import com.finance.transaction.domain.model.Transaccion;
import com.finance.transaction.domain.model.gateway.TransaccionGateway;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.TransaccionEntity;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.repository.TransaccionJpaRepository;
import com.finance.transaction.infraestructure.mapper.TransaccionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TransaccionAdapter implements TransaccionGateway {

    private final TransaccionJpaRepository jpaRepository;
    private final TransaccionMapper mapper;

    @Override
    public Transaccion guardarTransaccion(Transaccion transaction) {
        TransaccionEntity entity = mapper.transaccionToEntity(transaction);
        TransaccionEntity savedEntity = jpaRepository.save(entity);
        return mapper.entityToTransaccion(savedEntity);
    }

    @Override
    public Transaccion obtenerTransaccion(String id, String userId) {
        UUID txUuid = UUID.fromString(id);
        UUID userUuid = UUID.fromString(userId);

        return jpaRepository.findByIdAndUserId(txUuid, userUuid)
                .map(mapper::entityToTransaccion)
                .orElse(null);
    }

    @Override
    public Transaccion actualizarTransaccion(Transaccion transaccion) {
        UUID txUuid = UUID.fromString(transaccion.getId());
        UUID userUuid = UUID.fromString(transaccion.getExternalUserId());

        TransaccionEntity existente = jpaRepository.findByIdAndUserId(txUuid, userUuid)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada o no pertenece al usuario"));

        existente.setCategoryId(UUID.fromString(transaccion.getCategoryId()));
        existente.setAmount(transaccion.getAmount());
        existente.setCurrency(transaccion.getCurrency());
        existente.setType(transaccion.getType());
        existente.setFrequency(transaccion.getFrequency());
        existente.setDescription(transaccion.getDescription());
        existente.setTransactionDate(transaccion.getTransactionDate());

        TransaccionEntity transaccionActualizada = jpaRepository.save(existente);
        return mapper.entityToTransaccion(transaccionActualizada);
    }

    @Override
    @Transactional
    public void eliminarTransaccion(String id, String userId) {
        UUID txUuid = UUID.fromString(id);
        UUID userUuid = UUID.fromString(userId);

        jpaRepository.deleteByIdAndUserId(txUuid, userUuid);
    }

    @Override
    public List<Transaccion> obtenerTransaccionesPorUsuario(String userId) {
        UUID userUuid = UUID.fromString(userId);

        return jpaRepository.findByUserId(userUuid)
                .stream()
                .map(mapper::entityToTransaccion)
                .collect(Collectors.toList());
    }
}