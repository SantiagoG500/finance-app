package com.finance.notification.infrastructure.entrypoints.rest;

import com.finance.notification.infrastructure.entrypoints.rest.dto.AuthNotificationRequest;
import com.finance.notification.usecases.SendAuthNotificationUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final SendAuthNotificationUseCase sendAuthNotificationUseCase;

    public NotificationController(SendAuthNotificationUseCase sendAuthNotificationUseCase) {
        this.sendAuthNotificationUseCase = sendAuthNotificationUseCase;
    }

    @PostMapping("/auth-event")
    public ResponseEntity<String> sendAuthEventNotification(@RequestBody AuthNotificationRequest request) {
        if (request == null || request.email() == null || request.email().isBlank()) {
            return ResponseEntity.badRequest().body("Error: El correo electrónico es requerido.");
        }

        sendAuthNotificationUseCase.execute(request.email());

        return ResponseEntity.ok("Notificación enviada exitosamente a: " + request.email());
    }
}
