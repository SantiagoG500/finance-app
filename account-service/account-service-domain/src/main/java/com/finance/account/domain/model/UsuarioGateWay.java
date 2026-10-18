package com.finance.account.domain.model;

import java.util.UUID;

public interface UsuarioGateWay {

    Usuario guardarUsuario(Usuario usuario);

    Usuario obtenerUsuarioPorExternalId(UUID id);

    Usuario actualizarUsuario(Usuario usuario);

    void eliminarUsuarioPorExternalId(UUID id);

}
