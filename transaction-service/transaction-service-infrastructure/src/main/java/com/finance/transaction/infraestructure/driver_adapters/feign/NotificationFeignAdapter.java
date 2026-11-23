package com.finance.transaction.infraestructure.driver_adapters.feign;

import com.finance.transaction.domain.model.gateway.NotificationGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationFeignAdapter implements NotificationGateway {

    private final NotificationFeignClient notificationFeignClient;

    @Override
    public void notifyTransactionCreated(
            String email,
            String transactionType,
            String formattedAmount,
            String description
    ) {
        if (email == null || email.isBlank()) {
            return;
        }
        try {
            notificationFeignClient.sendTransactionEventNotification(Map.of(
                    "email", email,
                    "transactionType", transactionType,
                    "amount", formattedAmount,
                    "description", description
            ));
        } catch (Exception e) {
            log.warn("No se pudo enviar notificación de transacción a {}: {}", email, e.getMessage());
        }
    }

    @Override
    public void notifySavingsGoalAchieved(
            String email,
            String goalTitle,
            String progressSummary,
            String percentage
    ) {
        if (email == null || email.isBlank()) {
            return;
        }
        try {
            notificationFeignClient.sendGoalAchievedNotification(Map.of(
                    "email", email,
                    "goalTitle", goalTitle,
                    "progressSummary", progressSummary,
                    "percentage", percentage
            ));
        } catch (Exception e) {
            log.warn("No se pudo enviar notificación de meta alcanzada a {}: {}", email, e.getMessage());
        }
    }

    @Override
    public void notifyProjectionDeficit(
            String email,
            String monthsHorizon,
            String projectedBalance
    ) {
        if (email == null || email.isBlank()) {
            return;
        }
        try {
            notificationFeignClient.sendProjectionAlertNotification(Map.of(
                    "email", email,
                    "monthsHorizon", monthsHorizon,
                    "projectedBalance", projectedBalance
            ));
        } catch (Exception e) {
            log.warn("No se pudo enviar alerta de proyección negativa a {}: {}", email, e.getMessage());
        }
    }
}
