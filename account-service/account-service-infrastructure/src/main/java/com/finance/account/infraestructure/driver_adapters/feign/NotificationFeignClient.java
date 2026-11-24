package com.finance.account.infraestructure.driver_adapters.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "notification-service", url = "${services.notification.url}")
public interface NotificationFeignClient {

    @PostMapping("/api/v1/notifications/auth-event")
    void sendRegistrationNotification(@RequestBody Map<String, String> payload);

    @PostMapping("/api/v1/notifications/login-alert")
    void sendLoginAlertNotification(@RequestBody Map<String, String> payload);

    @PostMapping("/api/v1/notifications/password-changed")
    void sendPasswordChangedNotification(@RequestBody Map<String, String> payload);

    @PostMapping("/api/v1/notifications/account-status")
    void sendAccountStatusNotification(@RequestBody Map<String, String> payload);

}
