package com.finance.transaction.infraestructure.driver_adapters.jpa_repository.adapter;

import com.finance.transaction.domain.model.MetaAhorro;
import com.finance.transaction.domain.model.gateway.MetaAhorrosGateway;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.MetaAhorrosEntity;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.repository.MetaAhorrosJpaRepository;
import com.finance.transaction.infraestructure.mapper.MetaAhorrosMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MetaAhorrosAdapter implements MetaAhorrosGateway {

    private final MetaAhorrosJpaRepository jpaRepository;
    private final MetaAhorrosMapper mapper;

    @Override
    public MetaAhorro guardarMetaAhorros(MetaAhorro metaAhorro) {
        MetaAhorrosEntity entidad = mapper.toMetaAhorrosEntity(metaAhorro);
        MetaAhorrosEntity entidadGuardada = jpaRepository.save(entidad);
        return mapper.toMetaAhorro(entidadGuardada);
    }

    @Override
    public MetaAhorro obtenerMetaAhorros(String id, String userId) {
        UUID goalUuid = UUID.fromString(id);
        UUID userUuid = UUID.fromString(userId);

        return jpaRepository.findByIdAndUserId(goalUuid, userUuid)
                .map(mapper::toMetaAhorro)
                .orElse(null);
    }

    @Override
    public MetaAhorro actualizarMetaAhorros(MetaAhorro metaAhorro) {
        UUID goalUuid = UUID.fromString(metaAhorro.getId());
        UUID userUuid = UUID.fromString(metaAhorro.getExternalUserId());

        MetaAhorrosEntity existente = jpaRepository.findByIdAndUserId(goalUuid, userUuid)
                .orElseThrow(() -> new IllegalArgumentException("Meta de ahorro no encontrada o no pertenece al usuario"));

        existente.setTitle(metaAhorro.getTitle());
        existente.setTargetAmount(metaAhorro.getTargetAmount());
        existente.setCurrentAmount(metaAhorro.getCurrentAmount());
        existente.setMonthlyContribution(metaAhorro.getMonthlyContribution());
        existente.setTargetDate(metaAhorro.getTargetDate());
        existente.setCurrency(metaAhorro.getCurrency());

        MetaAhorrosEntity metaActualizada = jpaRepository.save(existente);
        return mapper.toMetaAhorro(metaActualizada);
    }

    @Override
    @Transactional
    public void eliminarMetaAhorros(String id, String userId) {
        UUID goalUuid = UUID.fromString(id);
        UUID userUuid = UUID.fromString(userId);

        jpaRepository.deleteByIdAndUserId(goalUuid, userUuid);
    }

    @Override
    public List<MetaAhorro> obtenerMetasPorUsuario(String userId) {
        UUID userUuid = UUID.fromString(userId);

        return jpaRepository.findByUserId(userUuid)
                .stream()
                .map(mapper::toMetaAhorro)
                .collect(Collectors.toList());
    }
}