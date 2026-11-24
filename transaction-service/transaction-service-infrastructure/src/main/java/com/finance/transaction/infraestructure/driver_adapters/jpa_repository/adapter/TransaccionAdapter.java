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
    @Transactional
    public Transaccion guardarTransaccion(Transaccion transaction) {
        TransaccionEntity entity = mapper.transaccionToEntity(transaction);
        TransaccionEntity savedEntity = jpaRepository.save(entity);
        return mapper.entityToTransaccion(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Transaccion obtenerTransaccion(String id, String userId) {
        UUID txUuid = parseUuid(id, "ID de transacción");
        UUID userUuid = parseUuid(userId, "ID de usuario");

        return jpaRepository.findByIdAndUserId(txUuid, userUuid)
                .map(mapper::entityToTransaccion)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaccion> obtenerTransaccionesPorUsuario(String userId) {
        UUID userUuid = parseUuid(userId, "ID de usuario");

        return jpaRepository.findByUserId(userUuid)
                .stream()
                .map(mapper::entityToTransaccion)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeTransaccionPorCategoriaYUsuario(String categoryId, String userId) {
        UUID categoryUuid = parseUuid(categoryId, "ID de categoría");
        UUID userUuid = parseUuid(userId, "ID de usuario");
        return jpaRepository.existsByCategoryIdAndUserId(categoryUuid, userUuid);
    }

    private UUID parseUuid(String value, String fieldName) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Formato UUID inválido para " + fieldName);
        }
    }
}
