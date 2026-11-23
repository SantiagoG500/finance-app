package com.finance.notification.infrastructure.entrypoints.rest;

import com.finance.notification.domain.usecase.SendAuthNotificationUseCase;
import com.finance.notification.infrastructure.entrypoints.rest.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SendAuthNotificationUseCase sendAuthNotificationUseCase;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnOkWhenSendAuthEventNotificationSucceeds() throws Exception {
        // Arrange
        String payload = "{\"email\":\"user@finance.com\"}";
        doNothing().when(sendAuthNotificationUseCase).execute("user@finance.com");

        // Act
        var resultActions = mockMvc.perform(post("/api/v1/notifications/auth-event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notificación enviada exitosamente a: user@finance.com"));
        verify(sendAuthNotificationUseCase).execute("user@finance.com");
    }

    @Test
    void shouldReturnOkWhenSendLoginAlertNotificationSucceeds() throws Exception {
        // Arrange
        String payload = "{\"email\":\"user@finance.com\"}";
        doNothing().when(sendAuthNotificationUseCase).notifyLoginAlert("user@finance.com");

        // Act
        var resultActions = mockMvc.perform(post("/api/v1/notifications/login-alert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Alerta de inicio de sesión enviada exitosamente a: user@finance.com"));
        verify(sendAuthNotificationUseCase).notifyLoginAlert("user@finance.com");
    }

    @Test
    void shouldReturnOkWhenSendPasswordChangedNotificationSucceeds() throws Exception {
        // Arrange
        String payload = "{\"email\":\"user@finance.com\"}";
        doNothing().when(sendAuthNotificationUseCase).notifyPasswordChanged("user@finance.com");

        // Act
        var resultActions = mockMvc.perform(post("/api/v1/notifications/password-changed")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Alerta de actualización de contraseña enviada exitosamente a: user@finance.com"));
        verify(sendAuthNotificationUseCase).notifyPasswordChanged("user@finance.com");
    }

    @Test
    void shouldReturnOkWhenSendTransactionEventNotificationSucceeds() throws Exception {
        // Arrange
        String payload = """
                {
                    "email": "user@finance.com",
                    "transactionType": "INGRESO",
                    "amount": "1500000 COP",
                    "description": "Salario mensual"
                }
                """;
        doNothing().when(sendAuthNotificationUseCase)
                .notifyTransactionEvent("user@finance.com", "INGRESO", "1500000 COP", "Salario mensual");

        // Act
        var resultActions = mockMvc.perform(post("/api/v1/notifications/transaction-event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notificación de movimiento financiero enviada exitosamente a: user@finance.com"));
        verify(sendAuthNotificationUseCase)
                .notifyTransactionEvent("user@finance.com", "INGRESO", "1500000 COP", "Salario mensual");
    }

    @Test
    void shouldReturnOkWhenSendGoalAchievedNotificationSucceeds() throws Exception {
        // Arrange
        String payload = """
                {
                    "email": "user@finance.com",
                    "goalTitle": "Viaje Europa",
                    "progressSummary": "10000000 / 10000000",
                    "percentage": "100.0%"
                }
                """;
        doNothing().when(sendAuthNotificationUseCase)
                .notifySavingsGoalAchieved("user@finance.com", "Viaje Europa", "10000000 / 10000000", "100.0%");

        // Act
        var resultActions = mockMvc.perform(post("/api/v1/notifications/goal-achieved")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notificación de meta de ahorro enviada exitosamente a: user@finance.com"));
        verify(sendAuthNotificationUseCase)
                .notifySavingsGoalAchieved("user@finance.com", "Viaje Europa", "10000000 / 10000000", "100.0%");
    }

    @Test
    void shouldReturnOkWhenSendAccountStatusNotificationSucceeds() throws Exception {
        // Arrange
        String payload = """
                {
                    "email": "user@finance.com",
                    "status": "DESACTIVADA"
                }
                """;
        doNothing().when(sendAuthNotificationUseCase)
                .notifyAccountStatusChanged("user@finance.com", "DESACTIVADA");

        // Act
        var resultActions = mockMvc.perform(post("/api/v1/notifications/account-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notificación de estado de cuenta enviada exitosamente a: user@finance.com"));
        verify(sendAuthNotificationUseCase).notifyAccountStatusChanged("user@finance.com", "DESACTIVADA");
    }

    @Test
    void shouldReturnOkWhenSendProjectionAlertNotificationSucceeds() throws Exception {
        // Arrange
        String payload = """
                {
                    "email": "user@finance.com",
                    "monthsHorizon": "6",
                    "projectedBalance": "-450000.0 COP"
                }
                """;
        doNothing().when(sendAuthNotificationUseCase)
                .notifyProjectionDeficit("user@finance.com", "6", "-450000.0 COP");

        // Act
        var resultActions = mockMvc.perform(post("/api/v1/notifications/projection-alert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Alerta de proyección financiera enviada exitosamente a: user@finance.com"));
        verify(sendAuthNotificationUseCase).notifyProjectionDeficit("user@finance.com", "6", "-450000.0 COP");
    }

    @Test
    void shouldReturnBadRequestWhenEmailValidationFails() throws Exception {
        // Arrange
        String invalidPayload = "{\"email\":\"not-an-email\"}";

        // Act
        var resultActions = mockMvc.perform(post("/api/v1/notifications/auth-event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload));

        // Assert
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("El formato del correo electrónico no es válido"));
    }

    @Test
    void shouldReturnBadRequestWhenUseCaseThrowsIllegalArgumentException() throws Exception {
        // Arrange
        String payload = "{\"email\":\"user@finance.com\"}";
        doThrow(new IllegalArgumentException("El formato del correo electrónico del destinatario no es válido"))
                .when(sendAuthNotificationUseCase).execute("user@finance.com");

        // Act
        var resultActions = mockMvc.perform(post("/api/v1/notifications/auth-event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        // Assert
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("El formato del correo electrónico del destinatario no es válido"));
    }

    @Test
    void shouldReturnBadRequestWhenJsonPayloadIsMalformed() throws Exception {
        // Arrange
        String malformedJson = "{\"email\": \"user@finance.com\"";

        // Act
        var resultActions = mockMvc.perform(post("/api/v1/notifications/auth-event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson));

        // Assert
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("El cuerpo de la solicitud JSON está malformado o es inválido"));
    }

    @Test
    void shouldReturnBadGatewayWhenUseCaseThrowsIllegalStateException() throws Exception {
        // Arrange
        String payload = "{\"email\":\"user@finance.com\"}";
        doThrow(new IllegalStateException("Error al construir el correo HTML para: user@finance.com"))
                .when(sendAuthNotificationUseCase).notifyLoginAlert("user@finance.com");

        // Act
        var resultActions = mockMvc.perform(post("/api/v1/notifications/login-alert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        // Assert
        resultActions.andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("Error al construir el correo HTML para: user@finance.com"));
    }

    @Test
    void shouldReturnInternalServerErrorWhenUnexpectedExceptionOccurs() throws Exception {
        // Arrange
        String payload = "{\"email\":\"user@finance.com\"}";
        doThrow(new RuntimeException("Unexpected SMTP crash"))
                .when(sendAuthNotificationUseCase).notifyPasswordChanged("user@finance.com");

        // Act
        var resultActions = mockMvc.perform(post("/api/v1/notifications/password-changed")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        // Assert
        resultActions.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Ha ocurrido un error interno inesperado en el servidor de notificaciones"));
    }
}
