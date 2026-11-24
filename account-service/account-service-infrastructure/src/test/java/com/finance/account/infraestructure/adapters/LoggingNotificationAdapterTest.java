package com.finance.account.infraestructure.adapters;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class LoggingNotificationAdapterTest {

    @Test
    void shouldExecuteAllLoggingNotificationMethodsWithoutError() {
        // Arrange
        LoggingNotificationAdapter adapter = new LoggingNotificationAdapter();
        String email = "log@example.com";

        // Act
        // Assert
        assertDoesNotThrow(() -> {
            adapter.notifyUserRegistration(email);
            adapter.notifyLoginAlert(email);
            adapter.notifyPasswordChanged(email);
            adapter.notifyAccountStatusChanged(email, "ACTIVADA");
        });
    }
}
