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
}