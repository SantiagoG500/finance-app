package com.finance.notification.domain.usecase;

import com.finance.notification.domain.gateway.NotificationSenderGateway;
import com.finance.notification.domain.model.Notification;
import com.finance.notification.domain.model.NotificationTemplate;

import java.util.Objects;

public class SendAuthNotificationUseCase {

    private final NotificationSenderGateway notificationSenderGateway;

    public SendAuthNotificationUseCase(NotificationSenderGateway notificationSenderGateway) {
        this.notificationSenderGateway = Objects.requireNonNull(
                notificationSenderGateway,
                "NotificationSenderGateway no puede ser nulo"
        );
    }

    public void execute(String email) {
        execute(email, NotificationTemplate.AUTH_WELCOME_VERIFICATION);
    }

    public void execute(String email, NotificationTemplate template) {
        Objects.requireNonNull(template, "La plantilla de notificación no puede ser nula");
        Notification notification = template.buildNotification(email, email);
        notificationSenderGateway.send(notification);
    }

    public void notifyLoginAlert(String email) {
        validateNotBlank(email, "El correo electrónico es obligatorio");
        Notification notification = NotificationTemplate.AUTH_LOGIN_ALERT.buildNotification(email, email, email);
        notificationSenderGateway.send(notification);
    }

    public void notifyPasswordChanged(String email) {
        validateNotBlank(email, "El correo electrónico es obligatorio");
        Notification notification = NotificationTemplate.AUTH_PASSWORD_CHANGED.buildNotification(email, email);
        notificationSenderGateway.send(notification);
    }

    public void notifyTransactionEvent(
            String email,
            String transactionType,
            String formattedAmount,
            String description
    ) {
        validateNotBlank(email, "El correo electrónico es obligatorio");
        validateNotBlank(transactionType, "El tipo de transacción es obligatorio");
        validateNotBlank(formattedAmount, "El monto de la transacción es obligatorio");
        validateNotBlank(description, "La descripción de la transacción es obligatoria");

        Notification notification = NotificationTemplate.TRANSACTION_ALERT.buildNotification(
                email,
                transactionType.trim(),
                formattedAmount.trim(),
                description.trim()
        );
        notificationSenderGateway.send(notification);
    }

    public void notifySavingsGoalAchieved(
            String email,
            String goalTitle,
            String progressSummary,
            String percentage
    ) {
        validateNotBlank(email, "El correo electrónico es obligatorio");
        validateNotBlank(goalTitle, "El título de la meta de ahorro es obligatorio");
        validateNotBlank(progressSummary, "El resumen de progreso es obligatorio");
        validateNotBlank(percentage, "El porcentaje completado es obligatorio");

        Notification notification = NotificationTemplate.SAVINGS_GOAL_MILESTONE.buildNotification(
                email,
                goalTitle.trim(),
                progressSummary.trim(),
                percentage.trim()
        );
        notificationSenderGateway.send(notification);
    }

    public void notifyAccountStatusChanged(String email, String statusLabel) {
        validateNotBlank(email, "El correo electrónico es obligatorio");
        validateNotBlank(statusLabel, "El estado de la cuenta es obligatorio");

        Notification notification = NotificationTemplate.ACCOUNT_STATUS_CHANGED.buildNotification(
                email,
                email.trim(),
                statusLabel.trim()
        );
        notificationSenderGateway.send(notification);
    }

    public void notifyProjectionDeficit(String email, String monthsHorizon, String projectedBalance) {
        validateNotBlank(email, "El correo electrónico es obligatorio");
        validateNotBlank(monthsHorizon, "El horizonte de meses es obligatorio");
        validateNotBlank(projectedBalance, "El saldo proyectado es obligatorio");

        String recommendation = "Revisa tus gastos recurrentes o ajusta tus aportes mensuales para recuperar un flujo de caja positivo.";
        Notification notification = NotificationTemplate.PROJECTION_DEFICIT_ALERT.buildNotification(
                email,
                monthsHorizon.trim(),
                projectedBalance.trim(),
                recommendation
        );
        notificationSenderGateway.send(notification);
    }

    private void validateNotBlank(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
