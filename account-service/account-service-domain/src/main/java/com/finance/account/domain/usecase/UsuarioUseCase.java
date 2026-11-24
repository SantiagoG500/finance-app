package com.finance.account.domain.usecase;

import com.finance.account.domain.gateway.NotificationGateway;
import com.finance.account.domain.gateway.PasswordEncoderGateway;
import com.finance.account.domain.model.Usuario;
import com.finance.account.domain.model.UsuarioGateWay;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

public class UsuarioUseCase {

    private static final Set<String> MONEDAS_SOPORTADAS = Set.of("USD", "COP", "EUR", "MXN");

    private final UsuarioGateWay uGateway;
    private final NotificationGateway notificationGateway;
    private final PasswordEncoderGateway passwordEncoderGateway;

    public UsuarioUseCase(UsuarioGateWay uGateway) {
        this(uGateway, null, null);
    }

    public UsuarioUseCase(UsuarioGateWay uGateway, NotificationGateway notificationGateway) {
        this(uGateway, notificationGateway, null);
    }

    public UsuarioUseCase(UsuarioGateWay uGateway,
                          NotificationGateway notificationGateway,
                          PasswordEncoderGateway passwordEncoderGateway) {
        this.uGateway = uGateway;
        this.notificationGateway = notificationGateway;
        this.passwordEncoderGateway = passwordEncoderGateway;
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

    public Usuario actualizarUsuario(UUID id,
                                     String email,
                                     String rawPassword,
                                     String fullName,
                                     String avatarUrl,
                                     String baseCurrency,
                                     Boolean isActive) {
        if (id == null
                || email == null || email.isBlank()
                || rawPassword == null || rawPassword.length() < 6
                || fullName == null || fullName.isBlank()
                || avatarUrl == null || avatarUrl.isBlank()
                || baseCurrency == null || baseCurrency.isBlank()
                || isActive == null) {
            throw new IllegalArgumentException("Todos los campos del usuario son obligatorios para la actualización completa");
        }

        String normalizedCurrency = baseCurrency.trim().toUpperCase();
        if (!MONEDAS_SOPORTADAS.contains(normalizedCurrency)) {
            throw new IllegalArgumentException("Moneda base no soportada");
        }

        Usuario existente = uGateway.obtenerUsuarioPorId(id);
        if (existente == null) {
            throw new NoSuchElementException("Usuario no encontrado");
        }

        String normalizedEmail = email.trim().toLowerCase();
        Usuario porEmail = uGateway.obtenerUsuarioPorEmail(normalizedEmail);
        if (porEmail != null && !id.equals(porEmail.getId())) {
            throw new IllegalStateException("Ya existe una cuenta registrada con este correo electrónico");
        }

        String encodedPassword = passwordEncoderGateway != null
                ? passwordEncoderGateway.encode(rawPassword)
                : rawPassword;

        existente.setEmail(normalizedEmail);
        existente.setPasswordHash(encodedPassword);
        existente.setFullName(fullName.trim());
        existente.setAvatarUrl(avatarUrl.trim());
        existente.setBaseCurrency(normalizedCurrency);
        existente.setIsActive(isActive);

        return uGateway.actualizarUsuario(existente);
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
            throw new NoSuchElementException("Usuario no encontrado");
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
            throw new NoSuchElementException("Usuario no encontrado");
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
