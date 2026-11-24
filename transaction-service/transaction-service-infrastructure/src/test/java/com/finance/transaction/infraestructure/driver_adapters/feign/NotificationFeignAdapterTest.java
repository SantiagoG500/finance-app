package com.finance.transaction.infraestructure.driver_adapters.feign;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationFeignAdapterTest {

    @Mock
    private NotificationFeignClient notificationFeignClient;

    @InjectMocks
    private NotificationFeignAdapter notificationFeignAdapter;

    @Test
    void shouldSendTransactionCreatedNotificationWhenEmailIsValid() {
        // Arrange
        String email = "user@finance.com";

        // Act
        notificationFeignAdapter.notifyTransactionCreated(email, "INCOME", "1000.00 USD", "Salario");

        // Assert
        verify(notificationFeignClient).sendTransactionEventNotification(Map.of(
                "email", email,
                "transactionType", "INCOME",
                "amount", "1000.00 USD",
                "description", "Salario"
        ));
    }

    @Test
    void shouldSkipTransactionCreatedNotificationWhenEmailIsNullOrBlank() {
        // Arrange
        String blankEmail = "  ";

        // Act
        notificationFeignAdapter.notifyTransactionCreated(null, "INCOME", "100", "Desc");
        notificationFeignAdapter.notifyTransactionCreated(blankEmail, "INCOME", "100", "Desc");

        // Assert
        verifyNoInteractions(notificationFeignClient);
    }

    @Test
    void shouldSwallowExceptionWhenTransactionCreatedNotificationFails() {
        // Arrange
        doThrow(new RuntimeException("Feign timeout"))
                .when(notificationFeignClient)
                .sendTransactionEventNotification(anyMap());

        // Act
        assertDoesNotThrow(() -> notificationFeignAdapter.notifyTransactionCreated(
                "user@finance.com",
                "EXPENSE",
                "50.00 USD",
                "Café"
        ));

        // Assert
        verify(notificationFeignClient).sendTransactionEventNotification(anyMap());
    }

    @Test
    void shouldSendGoalAchievedNotificationWhenEmailIsValid() {
        // Arrange
        String email = "user@finance.com";

        // Act
        notificationFeignAdapter.notifySavingsGoalAchieved(email, "Fondo", "1000 / 1000 USD", "100.0%");

        // Assert
        verify(notificationFeignClient).sendGoalAchievedNotification(Map.of(
                "email", email,
                "goalTitle", "Fondo",
                "progressSummary", "1000 / 1000 USD",
                "percentage", "100.0%"
        ));
    }

    @Test
    void shouldSkipGoalAchievedNotificationWhenEmailIsNullOrBlank() {
        // Arrange
        String blankEmail = "";

        // Act
        notificationFeignAdapter.notifySavingsGoalAchieved(null, "Fondo", "1000 / 1000 USD", "100%");
        notificationFeignAdapter.notifySavingsGoalAchieved(blankEmail, "Fondo", "1000 / 1000 USD", "100%");

        // Assert
        verifyNoInteractions(notificationFeignClient);
    }

    @Test
    void shouldSwallowExceptionWhenGoalAchievedNotificationFails() {
        // Arrange
        doThrow(new RuntimeException("Service down"))
                .when(notificationFeignClient)
                .sendGoalAchievedNotification(anyMap());

        // Act
        assertDoesNotThrow(() -> notificationFeignAdapter.notifySavingsGoalAchieved(
                "user@finance.com",
                "Fondo",
                "1000 / 1000 USD",
                "100.0%"
        ));

        // Assert
        verify(notificationFeignClient).sendGoalAchievedNotification(anyMap());
    }

    @Test
    void shouldSendProjectionDeficitNotificationWhenEmailIsValid() {
        // Arrange
        String email = "user@finance.com";

        // Act
        notificationFeignAdapter.notifyProjectionDeficit(email, "6", "-500.00");

        // Assert
        verify(notificationFeignClient).sendProjectionAlertNotification(Map.of(
                "email", email,
                "monthsHorizon", "6",
                "projectedBalance", "-500.00"
        ));
    }

    @Test
    void shouldSkipProjectionDeficitNotificationWhenEmailIsNullOrBlank() {
        // Arrange
        String blankEmail = "   ";

        // Act
        notificationFeignAdapter.notifyProjectionDeficit(null, "6", "-500.00");
        notificationFeignAdapter.notifyProjectionDeficit(blankEmail, "6", "-500.00");

        // Assert
        verifyNoInteractions(notificationFeignClient);
    }

    @Test
    void shouldSwallowExceptionWhenProjectionDeficitNotificationFails() {
        // Arrange
        doThrow(new RuntimeException("Connection error"))
                .when(notificationFeignClient)
                .sendProjectionAlertNotification(anyMap());

        // Act
        assertDoesNotThrow(() -> notificationFeignAdapter.notifyProjectionDeficit(
                "user@finance.com",
                "6",
                "-500.00"
        ));

        // Assert
        verify(notificationFeignClient).sendProjectionAlertNotification(anyMap());
    }
}
