package com.finance.account.infraestructure.mapper;

import com.finance.account.domain.model.Usuario;
import com.finance.account.infraestructure.driver_adapters.jpa_repository.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario toUser(UsuarioEntity entity) {
        return new Usuario(
            entity.getId(),
            entity.getExternalId(),
            entity.getEmail(),
            entity.getDisplayName(),
            ""
        );
    }

    public UsuarioEntity toEntity(Usuario usuario) {
        if (usuario == null) return null;

        UsuarioEntity entity = new UsuarioEntity();

        entity.setId(usuario.getId());
        entity.setExternalId(usuario.getExternalId());
        entity.setEmail(usuario.getEmail());
        entity.setDisplayName(usuario.getDisplayName());

        return entity;
    }
}
