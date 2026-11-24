package com.finance.transaction.infraestructure.driver_adapters.jpa_repository.adapter;

import com.finance.transaction.domain.model.MetaAhorro;
import com.finance.transaction.domain.model.PaginatedMetaAhorro;
import com.finance.transaction.domain.model.gateway.MetaAhorrosGateway;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.MetaAhorrosEntity;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.repository.MetaAhorrosJpaRepository;
import com.finance.transaction.infraestructure.mapper.MetaAhorrosMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
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
    @Transactional(readOnly = true)
    public MetaAhorro obtenerMetaAhorros(String id, String userId) {
        UUID goalUuid = parseUuid(id, "ID de meta de ahorro");
        UUID userUuid = parseUuid(userId, "ID de usuario");

        return jpaRepository.findByIdAndUserId(goalUuid, userUuid)
                .map(mapper::toMetaAhorro)
                .orElse(null);
    }

    @Override
    public MetaAhorro actualizarMetaAhorros(MetaAhorro metaAhorro) {
        UUID goalUuid = parseUuid(metaAhorro.getId(), "ID de meta de ahorro");
        UUID userUuid = parseUuid(metaAhorro.getExternalUserId(), "ID de usuario");

        MetaAhorrosEntity existente = jpaRepository.findByIdAndUserId(goalUuid, userUuid)
                .orElseThrow(() -> new NoSuchElementException("Meta de ahorro no encontrada o no pertenece al usuario"));

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
        UUID goalUuid = parseUuid(id, "ID de meta de ahorro");
        UUID userUuid = parseUuid(userId, "ID de usuario");

        if (jpaRepository.findByIdAndUserId(goalUuid, userUuid).isEmpty()) {
            throw new NoSuchElementException("Meta de ahorro no encontrada o no pertenece al usuario");
        }

        jpaRepository.deleteByIdAndUserId(goalUuid, userUuid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetaAhorro> obtenerMetasPorUsuario(String userId) {
        UUID userUuid = parseUuid(userId, "ID de usuario");

        return jpaRepository.findByUserId(userUuid)
                .stream()
                .map(mapper::toMetaAhorro)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedMetaAhorro obtenerMetasPaginadasPorUsuario(String externalUserId, int page, int size) {
        UUID userUuid = parseUuid(externalUserId, "ID de usuario");
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MetaAhorrosEntity> pageResult = jpaRepository.findByUserId(userUuid, pageRequest);
        List<MetaAhorro> content = pageResult.getContent()
                .stream()
                .map(mapper::toMetaAhorro)
                .collect(Collectors.toList());

        return new PaginatedMetaAhorro(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast()
        );
    }

    private UUID parseUuid(String value, String fieldName) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Formato UUID inválido para " + fieldName);
        }
    }
}
