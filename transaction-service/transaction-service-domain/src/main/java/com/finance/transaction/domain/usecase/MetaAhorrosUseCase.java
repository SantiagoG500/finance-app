package com.finance.transaction.domain.usecase;

import com.finance.transaction.domain.model.MetaAhorro;
import com.finance.transaction.domain.model.PaginatedMetaAhorro;
import com.finance.transaction.domain.model.VerifiedUserSession;
import com.finance.transaction.domain.model.gateway.AccountVerificationGateway;
import com.finance.transaction.domain.model.gateway.MetaAhorrosGateway;
import com.finance.transaction.domain.model.gateway.NotificationGateway;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class MetaAhorrosUseCase {

    private final MetaAhorrosGateway mGateway;
    private final NotificationGateway notificationGateway;
    private final AccountVerificationGateway accountVerificationGateway;

    public MetaAhorrosUseCase(MetaAhorrosGateway mGateway) {
        this(mGateway, null, null);
    }

    public MetaAhorrosUseCase(MetaAhorrosGateway mGateway, NotificationGateway notificationGateway) {
        this(mGateway, notificationGateway, null);
    }

    public MetaAhorrosUseCase(
            MetaAhorrosGateway mGateway,
            NotificationGateway notificationGateway,
            AccountVerificationGateway accountVerificationGateway
    ) {
        this.mGateway = mGateway;
        this.notificationGateway = notificationGateway;
        this.accountVerificationGateway = accountVerificationGateway;
    }

    public MetaAhorro guardarMetaAhorros(MetaAhorro metaAhorro) {
        return guardarMetaAhorros(metaAhorro, null);
    }

    public MetaAhorro guardarMetaAhorros(MetaAhorro metaAhorro, String bearerToken) {
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
        verificarSesionUsuario(metaAhorro.getExternalUserId(), bearerToken);
        return mGateway.guardarMetaAhorros(metaAhorro);
    }

    public MetaAhorro obtenerMetaAhorros(String id, String externalId) {
        return obtenerMetaAhorros(id, externalId, null);
    }

    public MetaAhorro obtenerMetaAhorros(String id, String externalId, String bearerToken) {
        validarParametros(id, externalId);
        verificarSesionUsuario(externalId, bearerToken);
        return mGateway.obtenerMetaAhorros(id, externalId);
    }

    public List<MetaAhorro> obtenerMetasPorUsuario(String externalId) {
        return obtenerMetasPorUsuario(externalId, null);
    }

    public List<MetaAhorro> obtenerMetasPorUsuario(String externalId, String bearerToken) {
        if (externalId == null || externalId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID externo del usuario no puede ser nulo o vacío.");
        }
        verificarSesionUsuario(externalId, bearerToken);
        return mGateway.obtenerMetasPorUsuario(externalId);
    }

    public PaginatedMetaAhorro obtenerMetasPaginadas(String externalUserId, String bearerToken, int page, int size) {
        if (externalUserId == null || externalUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID externo del usuario no puede ser nulo o vacío.");
        }
        if (page < 0) {
            throw new IllegalArgumentException("El número de página (page) debe ser mayor o igual a 0.");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("El tamaño de página (size) debe estar entre 1 y 100.");
        }
        verificarSesionUsuario(externalUserId, bearerToken);
        return mGateway.obtenerMetasPaginadasPorUsuario(externalUserId, page, size);
    }

    public MetaAhorro actualizarMetaAhorros(MetaAhorro metaAhorro) {
        return actualizarMetaAhorros(metaAhorro, null);
    }

    public MetaAhorro actualizarMetaAhorros(MetaAhorro metaAhorro, String bearerToken) {
        validarMetaAhorrosObj(metaAhorro);
        validarParametros(metaAhorro.getId(), metaAhorro.getExternalUserId());
        verificarSesionUsuario(metaAhorro.getExternalUserId(), bearerToken);
        return mGateway.actualizarMetaAhorros(metaAhorro);
    }

    public void eliminarMetaAhorros(String id, String externalId) {
        eliminarMetaAhorros(id, externalId, null);
    }

    public void eliminarMetaAhorros(String id, String externalId, String bearerToken) {
        validarParametros(id, externalId);
        verificarSesionUsuario(externalId, bearerToken);
        mGateway.eliminarMetaAhorros(id, externalId);
    }

    public MetaAhorro abonarMetaAhorros(String id, String externalId, BigDecimal montoAbono) {
        return abonarMetaAhorros(id, externalId, montoAbono, null, null);
    }

    public MetaAhorro abonarMetaAhorros(String id, String externalId, BigDecimal montoAbono, String userEmail) {
        return abonarMetaAhorros(id, externalId, montoAbono, userEmail, null);
    }

    public MetaAhorro abonarMetaAhorros(
            String id,
            String externalId,
            BigDecimal montoAbono,
            String userEmail,
            String bearerToken
    ) {
        validarParametros(id, externalId);
        if (montoAbono == null || montoAbono.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del abono debe ser mayor a cero");
        }
        verificarSesionUsuario(externalId, bearerToken);

        MetaAhorro meta = mGateway.obtenerMetaAhorros(id, externalId);
        if (meta == null) {
            throw new NoSuchElementException("Meta de ahorro no encontrada");
        }

        BigDecimal montoActual = meta.getCurrentAmount() != null ? meta.getCurrentAmount() : BigDecimal.ZERO;
        BigDecimal montoObjetivo = meta.getTargetAmount();

        if (montoObjetivo != null) {
            if (montoActual.compareTo(montoObjetivo) >= 0) {
                throw new IllegalStateException("La meta de ahorro ya alcanzó el 100% de su monto objetivo");
            }
            if (montoActual.add(montoAbono).compareTo(montoObjetivo) > 0) {
                throw new IllegalArgumentException("El monto del abono supera el saldo restante para completar la meta");
            }
        }

        meta.setCurrentAmount(montoActual.add(montoAbono));

        MetaAhorro actualizada = mGateway.actualizarMetaAhorros(meta);

        if (notificationGateway != null
                && userEmail != null
                && !userEmail.isBlank()
                && actualizada != null
                && actualizada.getCurrentAmount() != null
                && actualizada.getTargetAmount() != null
                && actualizada.getCurrentAmount().compareTo(actualizada.getTargetAmount()) >= 0) {
            try {
                String progressSummary = actualizada.getCurrentAmount() + " / " + actualizada.getTargetAmount() + " " + actualizada.getCurrency();
                String percentage = actualizada.calcularPorcentajeCompletado() + "%";
                notificationGateway.notifySavingsGoalAchieved(
                        userEmail,
                        actualizada.getTitle(),
                        progressSummary,
                        percentage
                );
            } catch (Exception ignored) {
            }
        }

        return actualizada;
    }

    private void verificarSesionUsuario(String externalUserId, String bearerToken) {
        if (accountVerificationGateway != null) {
            VerifiedUserSession session = accountVerificationGateway.verifyLoggedUser(externalUserId, bearerToken);
            if (session == null || session.id() == null || session.id().isBlank()) {
                throw new IllegalStateException("La sesión del usuario no pudo ser verificada");
            }
        }
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
