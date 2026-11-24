package com.finance.notification.domain.usecase;

import com.finance.notification.domain.gateway.NotificationSenderGateway;
import com.finance.notification.domain.model.Notification;
import com.finance.notification.domain.model.NotificationTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SendAuthNotificationUseCaseTest {

    @Mock
    private NotificationSenderGateway notificationSenderGateway;

    private SendAuthNotificationUseCase sendAuthNotificationUseCase;

    @BeforeEach
    void setUp() {
        sendAuthNotificationUseCase = new SendAuthNotificationUseCase(notificationSenderGateway);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenGatewayIsNull() {
        // Arrange
        NotificationSenderGateway nullGateway = null;

        // Act
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new SendAuthNotificationUseCase(nullGateway)
        );

        // Assert
        assertEquals("NotificationSenderGateway no puede ser nulo", exception.getMessage());
    }

    @Test
    void shouldSendWelcomeNotificationWhenExecuteIsCalledWithValidEmail() {
        // Arrange
        String email = "user@finance.com";
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        // Act
        sendAuthNotificationUseCase.execute(email);

        // Assert
        verify(notificationSenderGateway).send(captor.capture());
        Notification captured = captor.getValue();
        assertEquals(email, captured.recipient());
        assertEquals(NotificationTemplate.AUTH_WELCOME_VERIFICATION.getSubjectTemplate(), captured.subject());
        assertTrue(captured.body().contains(email));
    }

    @Test
    void shouldSendCustomTemplateNotificationWhenExecuteIsCalledWithTemplate() {
        // Arrange
        String email = "user@finance.com";
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        // Act
        sendAuthNotificationUseCase.execute(email, NotificationTemplate.AUTH_PASSWORD_CHANGED);

        // Assert
        verify(notificationSenderGateway).send(captor.capture());
        Notification captured = captor.getValue();
        assertEquals(email, captured.recipient());
        assertEquals(NotificationTemplate.AUTH_PASSWORD_CHANGED.getSubjectTemplate(), captured.subject());
        assertTrue(captured.body().contains(email));
    }

    @Test
    void shouldThrowNullPointerExceptionWhenTemplateIsNull() {
        // Arrange
        String email = "user@finance.com";

        // Act
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> sendAuthNotificationUseCase.execute(email, null)
        );

        // Assert
        assertEquals("La plantilla de notificación no puede ser nula", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenExecuteReceivesMalformedEmail() {
        // Arrange
        String malformedEmail = "invalid-email";

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> sendAuthNotificationUseCase.execute(malformedEmail)
        );

        // Assert
        assertTrue(exception.getMessage().contains("El formato del correo electrónico del destinatario no es válido"));
    }

    @Test
    void shouldSendLoginAlertWhenEmailIsValid() {
        // Arrange
        String email = "user@finance.com";
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        // Act
        sendAuthNotificationUseCase.notifyLoginAlert(email);

        // Assert
        verify(notificationSenderGateway).send(captor.capture());
        Notification captured = captor.getValue();
        assertEquals(email, captured.recipient());
        assertEquals(NotificationTemplate.AUTH_LOGIN_ALERT.getSubjectTemplate(), captured.subject());
        assertTrue(captured.body().contains(email));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenNotifyLoginAlertEmailIsBlank() {
        // Arrange
        String blankEmail = "   ";

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> sendAuthNotificationUseCase.notifyLoginAlert(blankEmail)
        );

        // Assert
        assertEquals("El correo electrónico es obligatorio", exception.getMessage());
    }

    @Test
    void shouldSendPasswordChangedNotificationWhenEmailIsValid() {
        // Arrange
        String email = "user@finance.com";
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        // Act
        sendAuthNotificationUseCase.notifyPasswordChanged(email);

        // Assert
        verify(notificationSenderGateway).send(captor.capture());
        Notification captured = captor.getValue();
        assertEquals(email, captured.recipient());
        assertEquals(NotificationTemplate.AUTH_PASSWORD_CHANGED.getSubjectTemplate(), captured.subject());
        assertTrue(captured.body().contains(email));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenNotifyPasswordChangedEmailIsNull() {
        // Arrange
        String nullEmail = null;

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> sendAuthNotificationUseCase.notifyPasswordChanged(nullEmail)
        );

        // Assert
        assertEquals("El correo electrónico es obligatorio", exception.getMessage());
    }

    @Test
    void shouldSendTransactionEventNotificationWhenAllParametersAreValid() {
        // Arrange
        String email = "user@finance.com";
        String transactionType = "INGRESO";
        String formattedAmount = "$1,500,000 COP";
        String description = "Pago de nómina";
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        // Act
        sendAuthNotificationUseCase.notifyTransactionEvent(email, transactionType, formattedAmount, description);

        // Assert
        verify(notificationSenderGateway).send(captor.capture());
        Notification captured = captor.getValue();
        assertEquals(email, captured.recipient());
        assertEquals(NotificationTemplate.TRANSACTION_ALERT.getSubjectTemplate(), captured.subject());
        assertTrue(captured.body().contains(transactionType));
        assertTrue(captured.body().contains(formattedAmount));
        assertTrue(captured.body().contains(description));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenNotifyTransactionEventParametersAreInvalid() {
        // Arrange
        String validEmail = "user@finance.com";

        // Act
        IllegalArgumentException emailException = assertThrows(
                IllegalArgumentException.class,
                () -> sendAuthNotificationUseCase.notifyTransactionEvent(null, "INGRESO", "100", "Desc")
        );
        IllegalArgumentException typeException = assertThrows(
                IllegalArgumentException.class,
                () -> sendAuthNotificationUseCase.notifyTransactionEvent(validEmail, "  ", "100", "Desc")
        );
        IllegalArgumentException amountException = assertThrows(
                IllegalArgumentException.class,
                () -> sendAuthNotificationUseCase.notifyTransactionEvent(validEmail, "INGRESO", "", "Desc")
        );
        IllegalArgumentException descException = assertThrows(
                IllegalArgumentException.class,
                () -> sendAuthNotificationUseCase.notifyTransactionEvent(validEmail, "INGRESO", "100", null)
        );

        // Assert
        assertEquals("El correo electrónico es obligatorio", emailException.getMessage());
        assertEquals("El tipo de transacción es obligatorio", typeException.getMessage());
        assertEquals("El monto de la transacción es obligatorio", amountException.getMessage());
        assertEquals("La descripción de la transacción es obligatoria", descException.getMessage());
    }

    @Test
    void shouldSendSavingsGoalAchievedNotificationWhenAllParametersAreValid() {
        // Arrange
        String email = "user@finance.com";
        String goalTitle = "Fondo de Emergencia";
        String progressSummary = "5000000 / 5000000";
        String percentage = "100%";
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        // Act
        sendAuthNotificationUseCase.notifySavingsGoalAchieved(email, goalTitle, progressSummary, percentage);

        // Assert
        verify(notificationSenderGateway).send(captor.capture());
        Notification captured = captor.getValue();
        assertEquals(email, captured.recipient());
        assertEquals(NotificationTemplate.SAVINGS_GOAL_MILESTONE.getSubjectTemplate(), captured.subject());
        assertTrue(captured.body().contains(goalTitle));
        assertTrue(captured.body().contains(progressSummary));
        assertTrue(captured.body().contains(percentage));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenNotifySavingsGoalAchievedParametersAreInvalid() {
        // Arrange
        String validEmail = "user@finance.com";

        // Act
        IllegalArgumentException emailException = assertThrows(
                IllegalArgumentException.class,
                () -> sendAuthNotificationUseCase.notifySavingsGoalAchieved("", "Meta", "100/100", "100%")
        );
        IllegalArgumentException titleException = assertThrows(
                IllegalArgumentException.class,
                () -> sendAuthNotificationUseCase.notifySavingsGoalAchieved(validEmail, null, "100/100", "100%")
        );
        IllegalArgumentException summaryException = assertThrows(
                IllegalArgumentException.class,
                () -> sendAuthNotificationUseCase.notifySavingsGoalAchieved(validEmail, "Meta", " ", "100%")
        );
        IllegalArgumentException percentageException = assertThrows(
                IllegalArgumentException.class,
                () -> sendAuthNotificationUseCase.notifySavingsGoalAchieved(validEmail, "Meta", "100/100", null)
        );

        // Assert
        assertEquals("El correo electrónico es obligatorio", emailException.getMessage());
        assertEquals("El título de la meta de ahorro es obligatorio", titleException.getMessage());
        assertEquals("El resumen de progreso es obligatorio", summaryException.getMessage());
        assertEquals("El porcentaje completado es obligatorio", percentageException.getMessage());
    }

    @Test
    void shouldSendAccountStatusChangedNotificationWhenParametersAreValid() {
        // Arrange
        String email = "user@finance.com";
        String statusLabel = "REACTIVADA";
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        // Act
        sendAuthNotificationUseCase.notifyAccountStatusChanged(email, statusLabel);

        // Assert
        verify(notificationSenderGateway).send(captor.capture());
        Notification captured = captor.getValue();
        assertEquals(email, captured.recipient());
        assertEquals(NotificationTemplate.ACCOUNT_STATUS_CHANGED.getSubjectTemplate(), captured.subject());
        assertTrue(captured.body().contains(statusLabel));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenNotifyAccountStatusChangedParametersAreInvalid() {
        // Arrange
        String validEmail = "user@finance.com";

        // Act
        IllegalArgumentException emailException = assertThrows(
                IllegalArgumentException.class,
                () -> sendAuthNotificationUseCase.notifyAccountStatusChanged(null, "ACTIVA")
        );
        IllegalArgumentException statusException = assertThrows(
                IllegalArgumentException.class,
                () -> sendAuthNotificationUseCase.notifyAccountStatusChanged(validEmail, "   ")
        );

        // Assert
        assertEquals("El correo electrónico es obligatorio", emailException.getMessage());
        assertEquals("El estado de la cuenta es obligatorio", statusException.getMessage());
    }

    @Test
    void shouldSendProjectionDeficitNotificationWhenParametersAreValid() {
        // Arrange
        String email = "user@finance.com";
        String monthsHorizon = "6";
        String projectedBalance = "-250000.0";
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        // Act
        sendAuthNotificationUseCase.notifyProjectionDeficit(email, monthsHorizon, projectedBalance);

        // Assert
        verify(notificationSenderGateway).send(captor.capture());
        Notification captured = captor.getValue();
        assertNotNull(captured);
        assertEquals(email, captured.recipient());
        assertEquals(NotificationTemplate.PROJECTION_DEFICIT_ALERT.getSubjectTemplate(), captured.subject());
        assertTrue(captured.body().contains(monthsHorizon));
        assertTrue(captured.body().contains(projectedBalance));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenNotifyProjectionDeficitParametersAreInvalid() {
        // Arrange
        String validEmail = "user@finance.com";

        // Act
        IllegalArgumentException emailException = assertThrows(
                IllegalArgumentException.class,
                () -> sendAuthNotificationUseCase.notifyProjectionDeficit(" ", "6", "-100")
        );
        IllegalArgumentException monthsException = assertThrows(
                IllegalArgumentException.class,
                () -> sendAuthNotificationUseCase.notifyProjectionDeficit(validEmail, null, "-100")
        );
        IllegalArgumentException balanceException = assertThrows(
                IllegalArgumentException.class,
                () -> sendAuthNotificationUseCase.notifyProjectionDeficit(validEmail, "6", "")
        );

        // Assert
        assertEquals("El correo electrónico es obligatorio", emailException.getMessage());
        assertEquals("El horizonte de meses es obligatorio", monthsException.getMessage());
        assertEquals("El saldo proyectado es obligatorio", balanceException.getMessage());
    }
}
