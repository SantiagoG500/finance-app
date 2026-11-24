package com.finance.account.infraestructure.adapters;

import com.finance.account.domain.gateway.NotificationGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingNotificationAdapter implements NotificationGateway {

    @Override
    public void notifyUserRegistration(String email) {
        log.info("Notificación de registro simulada para: {}", email);
    }

    @Override
    public void notifyLoginAlert(String email) {
        log.info("Alerta de inicio de sesión simulada para: {}", email);
    }

    @Override
    public void notifyPasswordChanged(String email) {
        log.info("Alerta de cambio de contraseña simulada para: {}", email);
    }

    @Override
    public void notifyAccountStatusChanged(String email, String status) {
        log.info("Notificación de cambio de estado ({}) simulada para: {}", status, email);
    }
}
