package com.finance.account.infraestructure.driver_adapters.jpa_repository;

import com.finance.account.domain.model.Usuario;
import com.finance.account.domain.model.UsuarioGateWay;
import com.finance.account.infraestructure.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UsuarioEntityGatewayImpl implements UsuarioGateWay {

    private final UsuarioEntityGatewayJpaRepository uJpaRepository;
    private final UsuarioMapper usuarioMapper;

    @Override
    public Usuario guardarUsuario(Usuario usuario) {
        UsuarioEntity usuarioEntity = usuarioMapper.toEntity(usuario);
        UsuarioEntity usuarioGuardado = uJpaRepository.save(usuarioEntity);

        return usuarioMapper.toUser(usuarioGuardado);
    }

    @Override
    public Usuario obtenerUsuarioPorExternalId(UUID id) {
        Optional<UsuarioEntity> usuarioEntity = uJpaRepository.findByExternalId(id);

        return usuarioEntity.map(usuarioMapper::toUser).orElse(null);
    }


    @Override
    public Usuario actualizarUsuario(Usuario usuario) {
        UsuarioEntity existente = uJpaRepository.findById(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        UsuarioEntity datosNuevos = usuarioMapper.toEntity(usuario);
        BeanUtils.copyProperties(datosNuevos, existente, "id");

        // 4. Guardar los cambios (Hibernate ejecutará un UPDATE automáticamente)
        UsuarioEntity usuarioActualizado = uJpaRepository.save(existente);

        return usuarioMapper.toUser(usuarioActualizado);
    }

    @Override
    public void eliminarUsuarioPorExternalId(UUID id) {
        uJpaRepository.deleteByExternalId(id);
    }
}
