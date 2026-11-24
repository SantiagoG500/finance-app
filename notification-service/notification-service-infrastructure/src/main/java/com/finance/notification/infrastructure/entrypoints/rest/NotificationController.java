package com.finance.notification.infrastructure.entrypoints.rest;

import com.finance.notification.domain.usecase.SendAuthNotificationUseCase;
import com.finance.notification.infrastructure.entrypoints.rest.dto.AccountStatusNotificationRequestDTO;
import com.finance.notification.infrastructure.entrypoints.rest.dto.AuthNotificationRequest;
import com.finance.notification.infrastructure.entrypoints.rest.dto.MessageResponseDTO;
import com.finance.notification.infrastructure.entrypoints.rest.dto.ProjectionAlertNotificationRequestDTO;
import com.finance.notification.infrastructure.entrypoints.rest.dto.SavingsGoalNotificationRequestDTO;
import com.finance.notification.infrastructure.entrypoints.rest.dto.TransactionNotificationRequestDTO;
import jakarta.validation.Valid;
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
    public ResponseEntity<MessageResponseDTO> sendAuthEventNotification(@Valid @RequestBody AuthNotificationRequest request) {
        sendAuthNotificationUseCase.execute(request.email());
        return ResponseEntity.ok(new MessageResponseDTO("Notificación enviada exitosamente a: " + request.email()));
    }

    @PostMapping("/login-alert")
    public ResponseEntity<MessageResponseDTO> sendLoginAlertNotification(@Valid @RequestBody AuthNotificationRequest request) {
        sendAuthNotificationUseCase.notifyLoginAlert(request.email());
        return ResponseEntity.ok(new MessageResponseDTO("Alerta de inicio de sesión enviada exitosamente a: " + request.email()));
    }

    @PostMapping("/password-changed")
    public ResponseEntity<MessageResponseDTO> sendPasswordChangedNotification(@Valid @RequestBody AuthNotificationRequest request) {
        sendAuthNotificationUseCase.notifyPasswordChanged(request.email());
        return ResponseEntity.ok(new MessageResponseDTO("Alerta de actualización de contraseña enviada exitosamente a: " + request.email()));
    }

    @PostMapping("/transaction-event")
    public ResponseEntity<MessageResponseDTO> sendTransactionEventNotification(
            @Valid @RequestBody TransactionNotificationRequestDTO request
    ) {
        sendAuthNotificationUseCase.notifyTransactionEvent(
                request.email(),
                request.transactionType(),
                request.amount(),
                request.description()
        );
        return ResponseEntity.ok(new MessageResponseDTO("Notificación de movimiento financiero enviada exitosamente a: " + request.email()));
    }

    @PostMapping("/goal-achieved")
    public ResponseEntity<MessageResponseDTO> sendGoalAchievedNotification(
            @Valid @RequestBody SavingsGoalNotificationRequestDTO request
    ) {
        sendAuthNotificationUseCase.notifySavingsGoalAchieved(
                request.email(),
                request.goalTitle(),
                request.progressSummary(),
                request.percentage()
        );
        return ResponseEntity.ok(new MessageResponseDTO("Notificación de meta de ahorro enviada exitosamente a: " + request.email()));
    }

    @PostMapping("/account-status")
    public ResponseEntity<MessageResponseDTO> sendAccountStatusNotification(
            @Valid @RequestBody AccountStatusNotificationRequestDTO request
    ) {
        sendAuthNotificationUseCase.notifyAccountStatusChanged(
                request.email(),
                request.status()
        );
        return ResponseEntity.ok(new MessageResponseDTO("Notificación de estado de cuenta enviada exitosamente a: " + request.email()));
    }

    @PostMapping("/projection-alert")
    public ResponseEntity<MessageResponseDTO> sendProjectionAlertNotification(
            @Valid @RequestBody ProjectionAlertNotificationRequestDTO request
    ) {
        sendAuthNotificationUseCase.notifyProjectionDeficit(
                request.email(),
                request.monthsHorizon(),
                request.projectedBalance()
        );
        return ResponseEntity.ok(new MessageResponseDTO("Alerta de proyección financiera enviada exitosamente a: " + request.email()));
    }
}
