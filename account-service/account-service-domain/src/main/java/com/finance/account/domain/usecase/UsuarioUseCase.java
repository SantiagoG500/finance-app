package com.finance.account.domain.usecase;

import com.finance.account.domain.gateway.NotificationGateway;
import com.finance.account.domain.model.Usuario;
import com.finance.account.domain.model.UsuarioGateWay;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

public class UsuarioUseCase {

    private static final Set<String> MONEDAS_SOPORTADAS = Set.of("USD", "COP", "EUR", "MXN");

    private final UsuarioGateWay uGateway;
    private final NotificationGateway notificationGateway;

    public UsuarioUseCase(UsuarioGateWay uGateway) {
        this(uGateway, null);
    }

    public UsuarioUseCase(UsuarioGateWay uGateway, NotificationGateway notificationGateway) {
        this.uGateway = uGateway;
        this.notificationGateway = notificationGateway;
    }

    public Usuario obtenerUsuarioPorId(UUID id) {
        Usuario usuario = uGateway.obtenerUsuarioPorId(id);
        validarUsuario(usuario);
        return usuario;
    }

    public Usuario obtenerUsuarioPorEmail(String email) {
        Usuario usuario = uGateway.obtenerUsuarioPorEmail(email);
        validarUsuario(usuario);
        return usuario;
    }

    public Usuario guardarUsuario(Usuario usuario) {
        validarUsuario(usuario);
        return uGateway.guardarUsuario(usuario);
    }

    public Usuario actualizarUsuario(Usuario usuario) {
        validarUsuario(usuario);
        return uGateway.actualizarUsuario(usuario);
    }

    public void eliminarUsuarioPorId(UUID id) {
        Usuario usuario = uGateway.obtenerUsuarioPorId(id);
        validarUsuario(usuario);
        uGateway.eliminarUsuarioPorId(id);
    }

    public Usuario cambiarEstadoCuenta(UUID id, Boolean active) {
        if (id == null || active == null) {
            throw new IllegalArgumentException("El ID y el estado activo no pueden ser nulos");
        }

        Usuario usuario = uGateway.obtenerUsuarioPorId(id);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        if (active.equals(usuario.getIsActive())) {
            throw new IllegalStateException("La cuenta ya se encuentra en el estado solicitado");
        }

        usuario.setIsActive(active);
        Usuario actualizado = uGateway.actualizarUsuario(usuario);

        if (notificationGateway != null && actualizado != null) {
            try {
                notificationGateway.notifyAccountStatusChanged(
                        actualizado.getEmail(),
                        Boolean.TRUE.equals(active) ? "ACTIVADA" : "DESACTIVADA"
                );
            } catch (Exception ignored) {
            }
        }

        return actualizado;
    }

    public Usuario actualizarMonedaBase(UUID id, String baseCurrency) {
        if (id == null || baseCurrency == null || baseCurrency.isBlank()) {
            throw new IllegalArgumentException("El ID y la moneda base no pueden ser nulos o vacíos");
        }

        String normalizedCurrency = baseCurrency.trim().toUpperCase();
        if (!MONEDAS_SOPORTADAS.contains(normalizedCurrency)) {
            throw new IllegalArgumentException("Moneda base no soportada");
        }

        Usuario usuario = uGateway.obtenerUsuarioPorId(id);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        usuario.setBaseCurrency(normalizedCurrency);
        return uGateway.actualizarUsuario(usuario);
    }

    public void validarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new NoSuchElementException("Usuario no puede ser null");
        }
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email no puede ser nulo o vacío.");
        }
    }
}
