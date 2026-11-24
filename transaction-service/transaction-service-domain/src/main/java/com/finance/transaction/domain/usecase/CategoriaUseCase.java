package com.finance.transaction.domain.usecase;

import com.finance.transaction.domain.model.Categoria;
import com.finance.transaction.domain.model.VerifiedUserSession;
import com.finance.transaction.domain.model.gateway.AccountVerificationGateway;
import com.finance.transaction.domain.model.gateway.CategoriaGateway;
import com.finance.transaction.domain.model.gateway.TransaccionGateway;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class CategoriaUseCase {

    private final CategoriaGateway categoriaGateway;
    private final TransaccionGateway transaccionGateway;
    private final AccountVerificationGateway accountVerificationGateway;

    public CategoriaUseCase(
            CategoriaGateway categoriaGateway,
            TransaccionGateway transaccionGateway,
            AccountVerificationGateway accountVerificationGateway
    ) {
        this.categoriaGateway = categoriaGateway;
        this.transaccionGateway = transaccionGateway;
        this.accountVerificationGateway = accountVerificationGateway;
    }

    public Categoria crearCategoria(Categoria categoria, String bearerToken) {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría no puede ser nula.");
        }
        if (categoria.getUserId() == null || categoria.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo o vacío.");
        }
        if (categoria.getName() == null || categoria.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede ser nulo o vacío.");
        }
        if (categoria.getType() == null) {
            throw new IllegalArgumentException("El tipo de la categoría no puede ser nulo.");
        }

        String normalizedUserId = categoria.getUserId().trim();
        String normalizedName = categoria.getName().trim();
        categoria.setUserId(normalizedUserId);
        categoria.setName(normalizedName);

        verificarSesionUsuario(normalizedUserId, bearerToken);

        if (categoriaGateway.existeCategoriaConNombreYTipo(normalizedUserId, normalizedName, categoria.getType())) {
            throw new IllegalStateException("Ya existe una categoría con ese nombre y tipo para el usuario");
        }

        return categoriaGateway.guardarCategoria(categoria);
    }

    public List<Categoria> listarCategoriasPorUsuario(String userId, String bearerToken) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo o vacío.");
        }
        String normalizedUserId = userId.trim();
        verificarSesionUsuario(normalizedUserId, bearerToken);
        List<Categoria> categorias = categoriaGateway.obtenerCategoriasPorUsuario(normalizedUserId);
        return categorias != null ? categorias : Collections.emptyList();
    }

    public void eliminarCategoria(String id, String userId, String bearerToken) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la categoría no puede ser nulo o vacío.");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo o vacío.");
        }

        String normalizedId = id.trim();
        String normalizedUserId = userId.trim();

        verificarSesionUsuario(normalizedUserId, bearerToken);

        categoriaGateway.obtenerCategoriaPorIdYUsuario(normalizedId, normalizedUserId)
                .orElseThrow(() -> new NoSuchElementException("Categoría no encontrada"));

        if (transaccionGateway != null
                && transaccionGateway.existeTransaccionPorCategoriaYUsuario(normalizedId, normalizedUserId)) {
            throw new IllegalStateException("No se puede eliminar la categoría porque tiene transacciones asociadas");
        }

        categoriaGateway.eliminarCategoria(normalizedId, normalizedUserId);
    }

    private void verificarSesionUsuario(String userId, String bearerToken) {
        if (accountVerificationGateway != null) {
            VerifiedUserSession session = accountVerificationGateway.verifyLoggedUser(userId, bearerToken);
            if (session == null || session.id() == null || session.id().isBlank()) {
                throw new IllegalStateException("La sesión del usuario no pudo ser verificada");
            }
        }
    }
}
