package com.finance.account.infraestructure.driver_adapters.jpa_repository;

import com.finance.account.domain.model.Usuario;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioEntityGatewayJpaRepository extends JpaRepository<UsuarioEntity, UUID> {

    Optional<UsuarioEntity> findByExternalId(UUID externalId);

    @Transactional
    void deleteByExternalId(UUID externalId);
}
