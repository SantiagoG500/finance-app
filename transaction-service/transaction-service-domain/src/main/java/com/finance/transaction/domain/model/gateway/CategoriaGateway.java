package com.finance.transaction.domain.model.gateway;

import com.finance.transaction.domain.model.Categoria;

public interface CategoriaGateway {

    Categoria guardarCategoria(Categoria categoria);

    Categoria obtenerCategoria(String id, String externalId);

    Categoria actualizarCategoria(Categoria categoria);

    void eliminarCategoria(String id, String externalId);

}
