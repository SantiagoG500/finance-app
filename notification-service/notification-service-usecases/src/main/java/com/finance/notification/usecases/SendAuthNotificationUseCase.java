package com.finance.notification.usecases;

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
}
