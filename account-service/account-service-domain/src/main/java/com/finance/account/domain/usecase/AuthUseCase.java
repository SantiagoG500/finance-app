package com.finance.account.domain.usecase;

import com.finance.account.domain.gateway.NotificationGateway;
import com.finance.account.domain.gateway.PasswordEncoderGateway;
import com.finance.account.domain.gateway.TokenGateway;
import com.finance.account.domain.model.AuthResult;
import com.finance.account.domain.model.Usuario;
import com.finance.account.domain.model.UsuarioGateWay;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class AuthUseCase {

    private final UsuarioGateWay usuarioGateWay;
    private final PasswordEncoderGateway passwordEncoderGateway;
    private final TokenGateway tokenGateway;
    private final NotificationGateway notificationGateway;

    public AuthResult register(String email, String rawPassword, String fullName) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (usuarioGateWay.obtenerUsuarioPorEmail(normalizedEmail) != null) {
            throw new IllegalArgumentException("Ya existe una cuenta registrada con este correo electrónico");
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

        try {
            notificationGateway.notifyUserRegistration(guardado.getEmail());
        } catch (Exception ignored) {
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

        if (Boolean.FALSE.equals(usuario.getIsActive())) {
            throw new IllegalStateException("La cuenta de usuario está desactivada");
        }

        if (!passwordEncoderGateway.matches(rawPassword, usuario.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String token = tokenGateway.generateToken(usuario.getId(), usuario.getEmail(), usuario.getFullName());
        return new AuthResult(token, usuario);
    }
}
