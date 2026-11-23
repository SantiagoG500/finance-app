package com.finance.transaction.domain.usecase;

import com.finance.transaction.domain.model.Transaccion;
import com.finance.transaction.domain.model.gateway.TransaccionGateway;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class TransaccionUseCase {

    private final TransaccionGateway tGateway;

    public Transaccion guardarTransaccion(Transaccion transaccion) {
        if (transaccion == null) {
            throw new IllegalArgumentException("La transacción no puede ser nula.");
        }
        if (transaccion.getCreatedAt() == null) {
            transaccion.setCreatedAt(LocalDateTime.now());
        }
        validarTransaccionObj(transaccion);
        return tGateway.guardarTransaccion(transaccion);
    }

    public Transaccion obtenerTransaccion(String id, String externalUserId) {
        validarParametros(id, externalUserId);
        return tGateway.obtenerTransaccion(id, externalUserId);
    }

    public Transaccion actualizarTransaccion(Transaccion transaccion) {
        validarTransaccionObj(transaccion);
        validarParametros(transaccion.getId(), transaccion.getExternalUserId());
        return tGateway.actualizarTransaccion(transaccion);
    }

    public void eliminarTransaccion(String id, String externalUserId) {
        validarParametros(id, externalUserId);
        tGateway.eliminarTransaccion(id, externalUserId);
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
