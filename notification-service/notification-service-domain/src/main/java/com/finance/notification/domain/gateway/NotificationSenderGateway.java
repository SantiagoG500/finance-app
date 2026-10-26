package com.finance.notification.domain.gateway;

import com.finance.notification.domain.model.Notification;

public interface NotificationSenderGateway {

    void send(Notification notification);
}
