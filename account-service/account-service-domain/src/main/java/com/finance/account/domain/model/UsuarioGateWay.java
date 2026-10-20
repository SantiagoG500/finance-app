package com.finance.account.domain.model;

public interface UsuarioGateWay {

    Usuario guardarUsuario(Usuario usuario);

    Usuario obtenerUsuarioPorExternalId(String id);

    Usuario actualizarUsuario(Usuario usuario);

    void eliminarUsuarioPorExternalId(String id);

}
