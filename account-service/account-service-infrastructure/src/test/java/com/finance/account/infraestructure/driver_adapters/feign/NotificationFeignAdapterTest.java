package com.finance.account.infraestructure.driver_adapters.feign;

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
    void shouldSendRegistrationNotificationWhenEmailIsValid() {
        // Arrange
        String email = "user@example.com";

        // Act
        notificationFeignAdapter.notifyUserRegistration(email);

        // Assert
        verify(notificationFeignClient).sendRegistrationNotification(Map.of("email", email));
    }

    @Test
    void shouldSkipRegistrationNotificationWhenEmailIsNullOrBlank() {
        // Arrange
        String blankEmail = "   ";

        // Act
        notificationFeignAdapter.notifyUserRegistration(null);
        notificationFeignAdapter.notifyUserRegistration(blankEmail);

        // Assert
        verifyNoInteractions(notificationFeignClient);
    }

    @Test
    void shouldSwallowExceptionWhenRegistrationNotificationFails() {
        // Arrange
        String email = "fail@example.com";
        doThrow(new RuntimeException("Feign timeout"))
                .when(notificationFeignClient)
                .sendRegistrationNotification(anyMap());

        // Act
        assertDoesNotThrow(() -> notificationFeignAdapter.notifyUserRegistration(email));

        // Assert
        verify(notificationFeignClient).sendRegistrationNotification(Map.of("email", email));
    }

    @Test
    void shouldSendLoginAlertWhenEmailIsValid() {
        // Arrange
        String email = "login@example.com";

        // Act
        notificationFeignAdapter.notifyLoginAlert(email);

        // Assert
        verify(notificationFeignClient).sendLoginAlertNotification(Map.of("email", email));
    }

    @Test
    void shouldSkipLoginAlertWhenEmailIsNullOrBlank() {
        // Arrange
        String blankEmail = "";

        // Act
        notificationFeignAdapter.notifyLoginAlert(null);
        notificationFeignAdapter.notifyLoginAlert(blankEmail);

        // Assert
        verifyNoInteractions(notificationFeignClient);
    }

    @Test
    void shouldSwallowExceptionWhenLoginAlertFails() {
        // Arrange
        String email = "fail@example.com";
        doThrow(new RuntimeException("Connection refused"))
                .when(notificationFeignClient)
                .sendLoginAlertNotification(anyMap());

        // Act
        assertDoesNotThrow(() -> notificationFeignAdapter.notifyLoginAlert(email));

        // Assert
        verify(notificationFeignClient).sendLoginAlertNotification(Map.of("email", email));
    }

    @Test
    void shouldSendPasswordChangedNotificationWhenEmailIsValid() {
        // Arrange
        String email = "pwd@example.com";

        // Act
        notificationFeignAdapter.notifyPasswordChanged(email);

        // Assert
        verify(notificationFeignClient).sendPasswordChangedNotification(Map.of("email", email));
    }

    @Test
    void shouldSkipPasswordChangedNotificationWhenEmailIsNullOrBlank() {
        // Arrange
        String blankEmail = " ";

        // Act
        notificationFeignAdapter.notifyPasswordChanged(null);
        notificationFeignAdapter.notifyPasswordChanged(blankEmail);

        // Assert
        verifyNoInteractions(notificationFeignClient);
    }

    @Test
    void shouldSwallowExceptionWhenPasswordChangedNotificationFails() {
        // Arrange
        String email = "fail@example.com";
        doThrow(new RuntimeException("Service unavailable"))
                .when(notificationFeignClient)
                .sendPasswordChangedNotification(anyMap());

        // Act
        assertDoesNotThrow(() -> notificationFeignAdapter.notifyPasswordChanged(email));

        // Assert
        verify(notificationFeignClient).sendPasswordChangedNotification(Map.of("email", email));
    }

    @Test
    void shouldSendAccountStatusNotificationWhenInputsAreValid() {
        // Arrange
        String email = "status@example.com";
        String status = "ACTIVADA";

        // Act
        notificationFeignAdapter.notifyAccountStatusChanged(email, status);

        // Assert
        verify(notificationFeignClient).sendAccountStatusNotification(Map.of(
                "email", email,
                "status", status
        ));
    }

    @Test
    void shouldSkipAccountStatusNotificationWhenEmailOrStatusIsNullOrBlank() {
        // Arrange
        String validEmail = "status@example.com";

        // Act
        notificationFeignAdapter.notifyAccountStatusChanged(null, "ACTIVADA");
        notificationFeignAdapter.notifyAccountStatusChanged(" ", "ACTIVADA");
        notificationFeignAdapter.notifyAccountStatusChanged(validEmail, null);
        notificationFeignAdapter.notifyAccountStatusChanged(validEmail, "  ");

        // Assert
        verifyNoInteractions(notificationFeignClient);
    }

    @Test
    void shouldSwallowExceptionWhenAccountStatusNotificationFails() {
        // Arrange
        String email = "fail@example.com";
        String status = "DESACTIVADA";
        doThrow(new RuntimeException("Feign error"))
                .when(notificationFeignClient)
                .sendAccountStatusNotification(anyMap());

        // Act
        assertDoesNotThrow(() -> notificationFeignAdapter.notifyAccountStatusChanged(email, status));

        // Assert
        verify(notificationFeignClient).sendAccountStatusNotification(Map.of(
                "email", email,
                "status", status
        ));
    }
}
