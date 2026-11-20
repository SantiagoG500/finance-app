package com.finance.account.domain.usecase;

import com.finance.account.domain.model.Usuario;
import com.finance.account.domain.model.UsuarioGateWay;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
public class UsuarioUseCase {
    private final UsuarioGateWay uGateway;

    public Usuario obtenerUsuarioPorId(UUID id) {
        Usuario usuario = uGateway.obtenerUsuarioPorId(id);
        validarUsuario(usuario);
        return usuario;
    }

    public Usuario obtenerUsuarioPorEmail(String email) {
        Usuario usuario = uGateway.obtenerUsuarioPorEmail(email);
        validarUsuario(usuario);
        return usuario;
    }

    public Usuario guardarUsuario(Usuario usuario) {
        validarUsuario(usuario);
        return uGateway.guardarUsuario(usuario);
    }

    public Usuario actualizarUsuario(Usuario usuario) {
        validarUsuario(usuario);
        return uGateway.actualizarUsuario(usuario);
    }

    public void eliminarUsuarioPorId(UUID id) {
        Usuario usuario = uGateway.obtenerUsuarioPorId(id);
        validarUsuario(usuario);
        uGateway.eliminarUsuarioPorId(id);
    }

    public void validarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new NoSuchElementException("Usuario no puede ser null");
        }
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email no puede ser nulo o vacío.");
        }
    }
}
