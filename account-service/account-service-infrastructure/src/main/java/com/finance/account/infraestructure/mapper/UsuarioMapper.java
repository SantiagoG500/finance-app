package com.finance.account.infraestructure.mapper;

import com.finance.account.domain.model.Usuario;
import com.finance.account.infraestructure.driver_adapters.jpa_repository.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario toUser(UsuarioEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Usuario(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getFullName(),
                entity.getAvatarUrl(),
                entity.getBaseCurrency(),
                entity.getIsActive(),
                entity.getCreatedAt()
        );
    }

    public UsuarioEntity toEntity(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(usuario.getId());
        entity.setEmail(usuario.getEmail());
        entity.setPasswordHash(usuario.getPasswordHash());
        entity.setFullName(usuario.getFullName());
        entity.setAvatarUrl(usuario.getAvatarUrl());
        entity.setBaseCurrency(usuario.getBaseCurrency());
        entity.setIsActive(usuario.getIsActive());
        entity.setCreatedAt(usuario.getCreatedAt());
        return entity;
    }
}
