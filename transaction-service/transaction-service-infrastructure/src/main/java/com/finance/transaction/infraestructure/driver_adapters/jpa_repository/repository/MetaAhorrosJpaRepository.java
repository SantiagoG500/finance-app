package com.finance.transaction.infraestructure.driver_adapters.jpa_repository.repository;

import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.MetaAhorrosEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetaAhorrosJpaRepository extends JpaRepository<MetaAhorrosEntity, UUID> {
    List<MetaAhorrosEntity> findByUserId(UUID userId);
    Optional<MetaAhorrosEntity> findByIdAndUserId(UUID id, UUID userId);
    void deleteByIdAndUserId(UUID id, UUID userId);
}