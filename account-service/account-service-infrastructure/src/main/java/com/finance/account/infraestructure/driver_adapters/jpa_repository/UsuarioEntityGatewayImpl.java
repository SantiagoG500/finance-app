package com.finance.account.infraestructure.driver_adapters.jpa_repository;

import com.finance.account.domain.model.Usuario;
import com.finance.account.domain.model.UsuarioGateWay;
import com.finance.account.infraestructure.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
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
    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorId(UUID id) {
        Optional<UsuarioEntity> usuarioEntity = uJpaRepository.findById(id);
        return usuarioEntity.map(usuarioMapper::toUser).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorEmail(String email) {
        Optional<UsuarioEntity> usuarioEntity = uJpaRepository.findByEmail(email);
        return usuarioEntity.map(usuarioMapper::toUser).orElse(null);
    }

    @Override
    public Usuario actualizarUsuario(Usuario usuario) {
        UsuarioEntity existente = uJpaRepository.findById(usuario.getId())
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        String previousPasswordHash = existente.getPasswordHash();
        UsuarioEntity datosNuevos = usuarioMapper.toEntity(usuario);
        BeanUtils.copyProperties(datosNuevos, existente, "id", "createdAt");
        if (datosNuevos.getPasswordHash() == null || datosNuevos.getPasswordHash().trim().isEmpty()) {
            existente.setPasswordHash(previousPasswordHash);
        }

        UsuarioEntity usuarioActualizado = uJpaRepository.save(existente);
        return usuarioMapper.toUser(usuarioActualizado);
    }

    @Override
    public void eliminarUsuarioPorId(UUID id) {
        uJpaRepository.deleteById(id);
    }
}
