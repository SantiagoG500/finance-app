package com.finance.transaction.domain.model.gateway;

import com.finance.transaction.domain.model.Categoria;
import com.finance.transaction.domain.model.enumns.TransaccionType;

import java.util.List;
import java.util.Optional;

public interface CategoriaGateway {

    Categoria guardarCategoria(Categoria categoria);

    List<Categoria> obtenerCategoriasPorUsuario(String userId);

    Optional<Categoria> obtenerCategoriaPorIdYUsuario(String id, String userId);

    boolean existeCategoriaConNombreYTipo(String userId, String name, TransaccionType type);

    void eliminarCategoria(String id, String userId);

}
