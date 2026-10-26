package com.finance.transaction.infraestructure.driver_adapters.jpa_repository.repository;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.MetaAhorrosEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetaAhorrosJpaRepository extends JpaRepository<MetaAhorrosEntity, String> {
    List<MetaAhorrosEntity> findByExternalUserId(String externalUserId);
}
