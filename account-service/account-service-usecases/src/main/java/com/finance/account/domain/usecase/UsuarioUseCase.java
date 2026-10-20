package com.finance.account.domain.usecase;

import com.finance.account.domain.model.Usuario;
import com.finance.account.domain.model.UsuarioGateWay;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class UsuarioUseCase {
    private final UsuarioGateWay uGateway;

    public Usuario obtenerUsuarioPorExternalId(String id) {
        System.out.println(">>> [USE CASE] Fetching user with ID: " + id);
        Usuario usuario = uGateway.obtenerUsuarioPorExternalId(id);

        System.out.println(">>> [USE CASE] User retrieved from DB gateway: " + usuario);
        validarUsuario(usuario);

        return usuario;
    }

    public Usuario actualizarUsuario(Usuario usuario) {
        validarUsuario(usuario);
        return uGateway.actualizarUsuario(usuario);
    }

    public void eliminarUsuarioPorExternalId(String id) {
        Usuario usuario =  uGateway.obtenerUsuarioPorExternalId(id);
        validarUsuario(usuario);
        uGateway.eliminarUsuarioPorExternalId(id);
    }

    public void validarUsuario (Usuario usuario) {
        if (usuario == null) {
            throw new NoSuchElementException("Usuario no puede ser null");
        }

//      TODO: See if it makes sense to have null ids during the data file cycle

        if (usuario.getEmail() == null){
            throw new IllegalArgumentException("Email no puede ser nulo.");
        }
        if (usuario.getDisplayName() == null){
            throw new IllegalArgumentException("Nombre no puede ser nulo.");
        }

    }
}
