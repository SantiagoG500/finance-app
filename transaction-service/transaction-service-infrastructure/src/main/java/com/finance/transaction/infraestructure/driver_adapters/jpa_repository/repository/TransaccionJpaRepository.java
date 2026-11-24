package com.finance.transaction.infraestructure.driver_adapters.jpa_repository.repository;

import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.TransaccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransaccionJpaRepository extends JpaRepository<TransaccionEntity, UUID> {
    List<TransaccionEntity> findByUserId(UUID userId);
    Optional<TransaccionEntity> findByIdAndUserId(UUID id, UUID userId);
    boolean existsByCategoryIdAndUserId(UUID categoryId, UUID userId);
}
