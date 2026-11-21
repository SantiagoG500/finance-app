package com.finance.account.domain.model;

import java.util.UUID;

public interface UsuarioGateWay {

    Usuario guardarUsuario(Usuario usuario);

    Usuario obtenerUsuarioPorId(UUID id);

    Usuario obtenerUsuarioPorEmail(String email);

    Usuario actualizarUsuario(Usuario usuario);

    void eliminarUsuarioPorId(UUID id);

}
