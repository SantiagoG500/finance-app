package com.finance.transaction.infraestructure.driver_adapters.jpa_repository.adapter;

import com.finance.transaction.domain.model.Categoria;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import com.finance.transaction.domain.model.gateway.CategoriaGateway;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.CategoriaEntity;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.repository.CategoriaJpaRepository;
import com.finance.transaction.infraestructure.mapper.CategoriaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CategoriaAdapter implements CategoriaGateway {

    private final CategoriaJpaRepository jpaRepository;
    private final CategoriaMapper mapper;

    @Override
    @Transactional
    public Categoria guardarCategoria(Categoria categoria) {
        CategoriaEntity entity = mapper.categoriaToEntity(categoria);
        CategoriaEntity savedEntity = jpaRepository.save(entity);
        return mapper.entityToCategoria(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> obtenerCategoriasPorUsuario(String userId) {
        UUID userUuid = parseUuid(userId, "ID de usuario");
        return jpaRepository.findByUserId(userUuid)
                .stream()
                .map(mapper::entityToCategoria)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Categoria> obtenerCategoriaPorIdYUsuario(String id, String userId) {
        UUID catUuid = parseUuid(id, "ID de categoría");
        UUID userUuid = parseUuid(userId, "ID de usuario");
        return jpaRepository.findByIdAndUserId(catUuid, userUuid)
                .map(mapper::entityToCategoria);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeCategoriaConNombreYTipo(String userId, String name, TransaccionType type) {
        UUID userUuid = parseUuid(userId, "ID de usuario");
        return jpaRepository.existsByUserIdAndNameIgnoreCaseAndType(userUuid, name, type);
    }

    @Override
    @Transactional
    public void eliminarCategoria(String id, String userId) {
        UUID catUuid = parseUuid(id, "ID de categoría");
        UUID userUuid = parseUuid(userId, "ID de usuario");
        jpaRepository.deleteByIdAndUserId(catUuid, userUuid);
    }

    private UUID parseUuid(String value, String fieldName) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Formato UUID inválido para " + fieldName);
        }
    }
}
