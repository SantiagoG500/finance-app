package com.finance.transaction.infraestructure.driver_adapters.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "notification-service", url = "${services.notification.url}")
public interface NotificationFeignClient {

    @PostMapping("/api/v1/notifications/transaction-event")
    void sendTransactionEventNotification(@RequestBody Map<String, String> payload);

    @PostMapping("/api/v1/notifications/goal-achieved")
    void sendGoalAchievedNotification(@RequestBody Map<String, String> payload);

    @PostMapping("/api/v1/notifications/projection-alert")
    void sendProjectionAlertNotification(@RequestBody Map<String, String> payload);

}
