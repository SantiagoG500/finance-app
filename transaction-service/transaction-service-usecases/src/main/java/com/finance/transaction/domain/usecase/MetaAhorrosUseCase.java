package com.finance.transaction.domain.usecase;

import com.finance.transaction.domain.model.MetaAhorro;
import com.finance.transaction.domain.model.gateway.MetaAhorrosGateway;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class MetaAhorrosUseCase {

    private final MetaAhorrosGateway mGateway;

    public MetaAhorro guardarMetaAhorros(MetaAhorro metaAhorro) {
        if (metaAhorro == null) {
            throw new IllegalArgumentException("La meta de ahorros no puede ser nula.");
        }
        if (metaAhorro.getId() == null || metaAhorro.getId().trim().isEmpty()) {
            metaAhorro.setId(UUID.randomUUID().toString());
        }
        if (metaAhorro.getCreatedAt() == null) {
            metaAhorro.setCreatedAt(LocalDateTime.now());
        }
        validarMetaAhorrosObj(metaAhorro);
        return mGateway.guardarMetaAhorros(metaAhorro);
    }

    public MetaAhorro obtenerMetaAhorros(String id, String externalId) {
        validarParametros(id, externalId);
        return mGateway.obtenerMetaAhorros(id, externalId);
    }

    public List<MetaAhorro> obtenerMetasPorUsuario(String externalId) {
        if (externalId == null || externalId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID externo del usuario no puede ser nulo o vacío.");
        }
        return mGateway.obtenerMetasPorUsuario(externalId);
    }

    public MetaAhorro actualizarMetaAhorros(MetaAhorro metaAhorro) {
        validarMetaAhorrosObj(metaAhorro);
        validarParametros(metaAhorro.getId(), metaAhorro.getExternalUserId());
        return mGateway.actualizarMetaAhorros(metaAhorro);
    }

    public void eliminarMetaAhorros(String id, String externalId) {
        validarParametros(id, externalId);
        mGateway.eliminarMetaAhorros(id, externalId);
    }

    // Validates the ID parameters used for fetching or deleting
    public void validarParametros(String id, String externalId) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la meta de ahorro no puede ser nulo o vacío.");
        }
        if (externalId == null || externalId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID externo del usuario no puede ser nulo o vacío.");
        }
    }

    // Validates the required fields of the MetaAhorro object
    public void validarMetaAhorrosObj(MetaAhorro meta) {
        if (meta == null) {
            throw new IllegalArgumentException("La meta de ahorros no puede ser nula.");
        }
        if (meta.getExternalUserId() == null || meta.getExternalUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario externo asociado no puede ser nulo o vacío.");
        }
        if (meta.getTitle() == null || meta.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("El título de la meta no puede ser nulo o vacío.");
        }
        if (meta.getTargetAmount() == null || meta.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto objetivo debe ser mayor a cero.");
        }
        if (meta.getCurrency() == null || meta.getCurrency().trim().isEmpty()) {
            throw new IllegalArgumentException("La moneda (currency) no puede ser nula o vacía.");
        }
        if (meta.getTargetDate() == null) {
            throw new IllegalArgumentException("La fecha objetivo no puede ser nula.");
        }

        // currentAmount and monthlyContribution might naturally default to zero, but they shouldn't be negative
        if (meta.getCurrentAmount() != null && meta.getCurrentAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto actual no puede ser negativo.");
        }
        if (meta.getMonthlyContribution() != null && meta.getMonthlyContribution().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El aporte mensual planificado no puede ser negativo.");
        }
    }
}
