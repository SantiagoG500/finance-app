package com.finance.transaction.domain.usecase;

import com.finance.transaction.domain.model.Categoria;
import com.finance.transaction.domain.model.gateway.CategoriaGateway;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CategoriaUseCase {

    private final CategoriaGateway cGateway;

    public Categoria guardarCategoria(Categoria categoria) {
        validarCategoriaObj(categoria);
        return cGateway.guardarCategoria(categoria);
    }

    public Categoria obtenerCategoria(String id, String externalId) {
        validarParametros(id, externalId);
        return cGateway.obtenerCategoria(id, externalId);
    }

    public Categoria actualizarCategoria(Categoria categoria) {
        validarCategoriaObj(categoria);
        validarParametros(categoria.getId(), categoria.getExternalUserId());
        return cGateway.actualizarCategoria(categoria);
    }

    public void eliminarCategoria(String id, String externalId) {
        validarParametros(id, externalId);
        cGateway.eliminarCategoria(id, externalId);
    }

    public void validarParametros(String id, String externalId) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la categoría no puede ser nulo o vacío.");
        }
        if (externalId == null || externalId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID externo del usuario no puede ser nulo o vacío.");
        }
    }

    public void validarCategoriaObj(Categoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría no puede ser nula.");
        }
        if (categoria.getExternalUserId() == null || categoria.getExternalUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario externo asociado no puede ser nulo o vacío.");
        }
        if (categoria.getName() == null || categoria.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede ser nulo o vacío.");
        }
        if (categoria.getType() == null) {
            throw new IllegalArgumentException("El tipo de categoría no puede ser nulo.");
        }
    }
}
