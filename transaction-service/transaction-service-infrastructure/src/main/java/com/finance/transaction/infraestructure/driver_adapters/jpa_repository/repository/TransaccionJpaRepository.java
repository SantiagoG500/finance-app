package com.finance.transaction.infraestructure.driver_adapters.jpa_repository.repository;

import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.TransaccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransaccionJpaRepository extends JpaRepository<TransaccionEntity, String> {
    List<TransaccionEntity> findByExternalUserId(String externalUserId);
}