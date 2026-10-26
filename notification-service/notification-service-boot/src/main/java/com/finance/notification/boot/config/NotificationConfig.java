package com.finance.notification.boot.config;

import com.finance.notification.domain.gateway.NotificationSenderGateway;
import com.finance.notification.usecases.SendAuthNotificationUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.finance.notification")
public class NotificationConfig {

    @Bean
    public SendAuthNotificationUseCase sendAuthNotificationUseCase(
            NotificationSenderGateway notificationSenderGateway
    ) {
        return new SendAuthNotificationUseCase(notificationSenderGateway);
    }
}
