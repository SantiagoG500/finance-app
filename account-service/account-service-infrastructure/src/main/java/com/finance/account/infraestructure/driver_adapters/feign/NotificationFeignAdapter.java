package com.finance.account.infraestructure.driver_adapters.feign;

import com.finance.account.domain.gateway.NotificationGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class NotificationFeignAdapter implements NotificationGateway {

    private final NotificationFeignClient notificationFeignClient;

    @Override
    public void notifyUserRegistration(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        try {
            notificationFeignClient.sendRegistrationNotification(Map.of("email", email));
        } catch (Exception e) {
            log.warn("No se pudo enviar notificación de registro a {}: {}", email, e.getMessage());
        }
    }

    @Override
    public void notifyLoginAlert(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        try {
            notificationFeignClient.sendLoginAlertNotification(Map.of("email", email));
        } catch (Exception e) {
            log.warn("No se pudo enviar alerta de inicio de sesión a {}: {}", email, e.getMessage());
        }
    }

    @Override
    public void notifyPasswordChanged(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        try {
            notificationFeignClient.sendPasswordChangedNotification(Map.of("email", email));
        } catch (Exception e) {
            log.warn("No se pudo enviar alerta de cambio de contraseña a {}: {}", email, e.getMessage());
        }
    }

    @Override
    public void notifyAccountStatusChanged(String email, String status) {
        if (email == null || email.isBlank() || status == null || status.isBlank()) {
            return;
        }
        try {
            notificationFeignClient.sendAccountStatusNotification(Map.of(
                    "email", email,
                    "status", status
            ));
        } catch (Exception e) {
            log.warn("No se pudo enviar notificación de estado de cuenta a {}: {}", email, e.getMessage());
        }
    }
}
