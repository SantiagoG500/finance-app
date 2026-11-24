package com.finance.transaction.domain.usecase;

import com.finance.transaction.domain.model.Categoria;
import com.finance.transaction.domain.model.Transaccion;
import com.finance.transaction.domain.model.VerifiedUserSession;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import com.finance.transaction.domain.model.gateway.AccountVerificationGateway;
import com.finance.transaction.domain.model.gateway.CategoriaGateway;
import com.finance.transaction.domain.model.gateway.NotificationGateway;
import com.finance.transaction.domain.model.gateway.TransaccionGateway;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TransaccionUseCase {

    private final TransaccionGateway tGateway;
    private final NotificationGateway notificationGateway;
    private final AccountVerificationGateway accountVerificationGateway;
    private final CategoriaGateway categoriaGateway;

    public TransaccionUseCase(TransaccionGateway tGateway) {
        this(tGateway, null, null, null);
    }

    public TransaccionUseCase(TransaccionGateway tGateway, NotificationGateway notificationGateway) {
        this(tGateway, notificationGateway, null, null);
    }

    public TransaccionUseCase(
            TransaccionGateway tGateway,
            NotificationGateway notificationGateway,
            AccountVerificationGateway accountVerificationGateway
    ) {
        this(tGateway, notificationGateway, accountVerificationGateway, null);
    }

    public TransaccionUseCase(
            TransaccionGateway tGateway,
            NotificationGateway notificationGateway,
            AccountVerificationGateway accountVerificationGateway,
            CategoriaGateway categoriaGateway
    ) {
        this.tGateway = tGateway;
        this.notificationGateway = notificationGateway;
        this.accountVerificationGateway = accountVerificationGateway;
        this.categoriaGateway = categoriaGateway;
    }

    public Transaccion crearTransaccion(Transaccion transaccion, String userEmail, String bearerToken) {
        return guardarTransaccion(transaccion, userEmail, bearerToken);
    }

    public Transaccion guardarTransaccion(Transaccion transaccion) {
        return guardarTransaccion(transaccion, null, null);
    }

    public Transaccion guardarTransaccion(Transaccion transaccion, String userEmail) {
        return guardarTransaccion(transaccion, userEmail, null);
    }

    public Transaccion guardarTransaccion(Transaccion transaccion, String userEmail, String bearerToken) {
        if (transaccion == null) {
            throw new IllegalArgumentException("La transacción no puede ser nula.");
        }
        if (transaccion.getCreatedAt() == null) {
            transaccion.setCreatedAt(LocalDateTime.now());
        }
        validarTransaccionObj(transaccion);
        verificarSesionUsuario(transaccion.getExternalUserId(), bearerToken);
        validarCategoriaAsociada(transaccion);
        Transaccion guardada = tGateway.guardarTransaccion(transaccion);

        if (notificationGateway != null && userEmail != null && !userEmail.isBlank() && guardada != null) {
            try {
                String formattedAmount = guardada.getAmount() + " " + guardada.getCurrency();
                String description = (guardada.getDescription() != null && !guardada.getDescription().isBlank())
                        ? guardada.getDescription()
                        : "Sin descripción";
                notificationGateway.notifyTransactionCreated(
                        userEmail,
                        guardada.getType().name(),
                        formattedAmount,
                        description
                );
            } catch (Exception ignored) {
            }
        }

        return guardada;
    }

    private void validarCategoriaAsociada(Transaccion transaccion) {
        if (categoriaGateway != null
                && transaccion.getCategoryId() != null
                && !transaccion.getCategoryId().trim().isEmpty()) {
            Categoria categoria = categoriaGateway.obtenerCategoriaPorIdYUsuario(
                    transaccion.getCategoryId().trim(),
                    transaccion.getExternalUserId().trim()
            ).orElseThrow(() -> new IllegalArgumentException(
                    "La categoría especificada no existe o no pertenece al usuario"
            ));

            if (categoria.getType() != transaccion.getType()) {
                throw new IllegalArgumentException(
                        "El tipo de la transacción no coincide con el tipo de la categoría"
                );
            }
        }
    }

    public Transaccion obtenerTransaccion(String id, String externalUserId) {
        return obtenerTransaccion(id, externalUserId, null);
    }

    public Transaccion obtenerTransaccion(String id, String externalUserId, String bearerToken) {
        validarParametros(id, externalUserId);
        verificarSesionUsuario(externalUserId, bearerToken);
        return tGateway.obtenerTransaccion(id, externalUserId);
    }

    public List<Transaccion> obtenerTransaccionesPorTipo(String externalUserId, TransaccionType type) {
        return obtenerTransaccionesPorTipo(externalUserId, type, null);
    }

    public List<Transaccion> obtenerTransaccionesPorTipo(
            String externalUserId,
            TransaccionType type,
            String bearerToken
    ) {
        if (externalUserId == null || externalUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID externo del usuario no puede ser nulo o vacío.");
        }
        if (type == null) {
            throw new IllegalArgumentException("El tipo de transacción no puede ser nulo.");
        }

        verificarSesionUsuario(externalUserId, bearerToken);

        List<Transaccion> transacciones = tGateway.obtenerTransaccionesPorUsuario(externalUserId);
        if (transacciones == null || transacciones.isEmpty()) {
            return Collections.emptyList();
        }

        return transacciones.stream()
                .filter(t -> t != null && t.getType() == type)
                .collect(Collectors.toList());
    }

    private void verificarSesionUsuario(String externalUserId, String bearerToken) {
        if (accountVerificationGateway != null) {
            VerifiedUserSession session = accountVerificationGateway.verifyLoggedUser(externalUserId, bearerToken);
            if (session == null || session.id() == null || session.id().isBlank()) {
                throw new IllegalStateException("La sesión del usuario no pudo ser verificada");
            }
        }
    }

    public void validarParametros(String id, String externalUserId) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la transacción no puede ser nulo o vacío.");
        }
        if (externalUserId == null || externalUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID externo del usuario no puede ser nulo o vacío.");
        }
    }

    public void validarTransaccionObj(Transaccion transaccion) {
        if (transaccion == null) {
            throw new IllegalArgumentException("La transacción no puede ser nula.");
        }
        if (transaccion.getExternalUserId() == null || transaccion.getExternalUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario externo asociado no puede ser nulo o vacío.");
        }
        if (transaccion.getCategoryId() == null || transaccion.getCategoryId().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la categoría no puede ser nulo o vacío.");
        }
        if (transaccion.getAmount() == null || transaccion.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de la transacción debe ser mayor a cero.");
        }
        if (transaccion.getCurrency() == null || transaccion.getCurrency().trim().isEmpty()) {
            throw new IllegalArgumentException("La moneda no puede ser nula o vacía.");
        }
        if (transaccion.getType() == null) {
            throw new IllegalArgumentException("El tipo de transacción no puede ser nulo.");
        }
        if (transaccion.getFrequency() == null) {
            throw new IllegalArgumentException("La frecuencia no puede ser nula.");
        }
        if (transaccion.getTransactionDate() == null) {
            throw new IllegalArgumentException("La fecha de transacción no puede ser nula.");
        }
    }
}
