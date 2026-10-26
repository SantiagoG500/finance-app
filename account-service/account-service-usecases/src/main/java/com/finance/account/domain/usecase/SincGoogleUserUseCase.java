package com.finance.account.domain.usecase;

import com.finance.account.domain.gateway.NotificationGateway;
import com.finance.account.domain.model.Usuario;
import com.finance.account.domain.model.UsuarioGateWay;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class SincGoogleUserUseCase {
    private final UsuarioGateWay usuarioGateWay;
    private final NotificationGateway notificationGateway;

    public Usuario iniciar(String externalId, String email, String displayName) {
        Usuario usuarioEncontrado = usuarioGateWay.obtenerUsuarioPorExternalId(externalId);

        if (usuarioEncontrado != null) return usuarioEncontrado;

        Usuario nuevoUsuario = new Usuario(
                UUID.randomUUID(),
                externalId,
                email,
                displayName,
                "COP"
        );

        Usuario usuarioGuardado = usuarioGateWay.guardarUsuario(nuevoUsuario);

        try {
            notificationGateway.notifyUserRegistration(email);
        } catch (Exception ignored) {
        }

        return usuarioGuardado;
    }
}
