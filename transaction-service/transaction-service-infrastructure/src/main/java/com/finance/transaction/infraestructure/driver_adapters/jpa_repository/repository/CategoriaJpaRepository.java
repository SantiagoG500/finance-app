package com.finance.transaction.infraestructure.driver_adapters.jpa_repository.repository;

import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.CategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoriaJpaRepository extends JpaRepository<CategoriaEntity, UUID> {

    List<CategoriaEntity> findByUserIdOrUserIdIsNull(UUID userId);
}