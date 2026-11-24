package com.finance.transaction.infraestructure.driver_adapters.jpa_repository.repository;

import com.finance.transaction.domain.model.enumns.TransaccionType;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.CategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaJpaRepository extends JpaRepository<CategoriaEntity, UUID> {

    List<CategoriaEntity> findByUserId(UUID userId);

    Optional<CategoriaEntity> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndNameIgnoreCaseAndType(UUID userId, String name, TransaccionType type);

    void deleteByIdAndUserId(UUID id, UUID userId);

}
