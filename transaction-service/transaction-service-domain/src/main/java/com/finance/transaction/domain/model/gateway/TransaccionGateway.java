package com.finance.transaction.domain.model.gateway;

import com.finance.transaction.domain.model.Transaccion;

import java.util.List;

public interface TransaccionGateway {

    Transaccion guardarTransaccion(Transaccion transaccion);

    Transaccion obtenerTransaccion(String id, String externalId);

    Transaccion actualizarTransaccion(Transaccion transaccion);

    void eliminarTransaccion(String id, String externalId);

    List<Transaccion> obtenerTransaccionesPorUsuario(String externalId);

}
