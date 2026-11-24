package com.finance.account.domain.usecase;

import com.finance.account.domain.gateway.NotificationGateway;
import com.finance.account.domain.gateway.PasswordEncoderGateway;
import com.finance.account.domain.gateway.TokenBlacklistGateway;
import com.finance.account.domain.gateway.TokenGateway;
import com.finance.account.domain.model.AuthResult;
import com.finance.account.domain.model.Usuario;
import com.finance.account.domain.model.UsuarioGateWay;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

public class AuthUseCase {

    private final UsuarioGateWay usuarioGateWay;
    private final PasswordEncoderGateway passwordEncoderGateway;
    private final TokenGateway tokenGateway;
    private final NotificationGateway notificationGateway;
    private final TokenBlacklistGateway tokenBlacklistGateway;

    public AuthUseCase(UsuarioGateWay usuarioGateWay,
                       PasswordEncoderGateway passwordEncoderGateway,
                       TokenGateway tokenGateway,
                       NotificationGateway notificationGateway) {
        this(usuarioGateWay, passwordEncoderGateway, tokenGateway, notificationGateway, null);
    }

    public AuthUseCase(UsuarioGateWay usuarioGateWay,
                       PasswordEncoderGateway passwordEncoderGateway,
                       TokenGateway tokenGateway,
                       NotificationGateway notificationGateway,
                       TokenBlacklistGateway tokenBlacklistGateway) {
        this.usuarioGateWay = usuarioGateWay;
        this.passwordEncoderGateway = passwordEncoderGateway;
        this.tokenGateway = tokenGateway;
        this.notificationGateway = notificationGateway;
        this.tokenBlacklistGateway = tokenBlacklistGateway;
    }

    public AuthResult register(String email, String rawPassword, String fullName) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (usuarioGateWay.obtenerUsuarioPorEmail(normalizedEmail) != null) {
            throw new IllegalStateException("Ya existe una cuenta registrada con este correo electrónico");
        }

        String encodedPassword = passwordEncoderGateway.encode(rawPassword);
        Usuario usuario = new Usuario(
                UUID.randomUUID(),
                normalizedEmail,
                encodedPassword,
                fullName,
                null,
                "USD",
                true
        );

        Usuario guardado = usuarioGateWay.guardarUsuario(usuario);

        if (notificationGateway != null) {
            try {
                notificationGateway.notifyUserRegistration(guardado.getEmail());
            } catch (Exception ignored) {
            }
        }

        String token = tokenGateway.generateToken(guardado.getId(), guardado.getEmail(), guardado.getFullName());
        return new AuthResult(token, guardado);
    }

    public AuthResult login(String email, String rawPassword) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("El email y la contraseña son obligatorios");
        }

        String normalizedEmail = email.trim().toLowerCase();
        Usuario usuario = usuarioGateWay.obtenerUsuarioPorEmail(normalizedEmail);
        if (usuario == null) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        if (!passwordEncoderGateway.matches(rawPassword, usuario.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        if (tokenBlacklistGateway != null && tokenBlacklistGateway.hasActiveSession(normalizedEmail)) {
            tokenBlacklistGateway.revokeActiveSessionByEmail(normalizedEmail);
        }

        String token = tokenGateway.generateToken(usuario.getId(), usuario.getEmail(), usuario.getFullName());
        Instant expiresAt = tokenGateway.extractExpiration(token);
        if (tokenBlacklistGateway != null) {
            tokenBlacklistGateway.registerActiveSession(normalizedEmail, token, expiresAt);
        }

        if (notificationGateway != null) {
            try {
                notificationGateway.notifyLoginAlert(usuario.getEmail());
            } catch (Exception ignored) {
            }
        }

        return new AuthResult(token, usuario);
    }

    public void changePassword(String email, String currentPassword, String newPassword) {
        if (email == null || email.isBlank()
                || currentPassword == null || currentPassword.isBlank()
                || newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("El email, la contraseña actual y la nueva contraseña no pueden ser nulos o vacíos");
        }
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres");
        }
        if (newPassword.equals(currentPassword)) {
            throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la contraseña actual");
        }

        String normalizedEmail = email.trim().toLowerCase();
        Usuario usuario = usuarioGateWay.obtenerUsuarioPorEmail(normalizedEmail);
        if (usuario == null) {
            throw new NoSuchElementException("Usuario no encontrado");
        }

        if (!Boolean.TRUE.equals(usuario.getIsActive())) {
            throw new IllegalStateException("La cuenta de usuario está desactivada");
        }

        if (!passwordEncoderGateway.matches(currentPassword, usuario.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        String encodedNewPassword = passwordEncoderGateway.encode(newPassword);
        usuario.setPasswordHash(encodedNewPassword);
        usuarioGateWay.actualizarUsuario(usuario);

        if (notificationGateway != null) {
            try {
                notificationGateway.notifyPasswordChanged(usuario.getEmail());
            } catch (Exception ignored) {
            }
        }
    }

    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new IllegalArgumentException("El token de autorización es obligatorio");
        }

        String trimmed = authorizationHeader.trim();
        String token;
        if (trimmed.equalsIgnoreCase("Bearer")) {
            token = "";
        } else if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = trimmed.substring(7).trim();
        } else {
            token = trimmed;
        }

        if (token.isEmpty() || !tokenGateway.validateToken(token)) {
            throw new IllegalArgumentException("Token JWT inválido o expirado");
        }

        Instant expiresAt = tokenGateway.extractExpiration(token);
        if (tokenBlacklistGateway != null) {
            tokenBlacklistGateway.revokeToken(token, expiresAt);
            tokenBlacklistGateway.clearActiveSessionByToken(token);
        }
    }
}
