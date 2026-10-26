package com.finance.transaction.domain.model.gateway;

import com.finance.transaction.domain.model.MetaAhorro;

import java.util.List;

public interface MetaAhorrosGateway {

    MetaAhorro guardarMetaAhorros(MetaAhorro metaAhorro);

    MetaAhorro obtenerMetaAhorros(String id, String externalId);

    MetaAhorro actualizarMetaAhorros(MetaAhorro metaAhorro);

    void eliminarMetaAhorros(String id, String externalId);

    List<MetaAhorro> obtenerMetasPorUsuario(String externalId);

}
