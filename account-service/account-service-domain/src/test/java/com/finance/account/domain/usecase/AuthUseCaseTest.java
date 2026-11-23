package com.finance.account.domain.usecase;

import com.finance.account.domain.gateway.NotificationGateway;
import com.finance.account.domain.gateway.PasswordEncoderGateway;
import com.finance.account.domain.gateway.TokenGateway;
import com.finance.account.domain.model.AuthResult;
import com.finance.account.domain.model.Usuario;
import com.finance.account.domain.model.UsuarioGateWay;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseTest {

    @Mock
    private UsuarioGateWay usuarioGateWay;

    @Mock
    private PasswordEncoderGateway passwordEncoderGateway;

    @Mock
    private TokenGateway tokenGateway;

    @Mock
    private NotificationGateway notificationGateway;

    @InjectMocks
    private AuthUseCase authUseCase;

    @Test
    void shouldRegisterUserSuccessfullyWhenDataIsValid() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String rawEmail = "  TestUser@Example.com ";
        String normalizedEmail = "testuser@example.com";
        String rawPassword = "password123";
        String encodedPassword = "encoded_password_123";
        String fullName = "Test User";
        Usuario savedUser = new Usuario(userId, normalizedEmail, encodedPassword, fullName, null, "USD", true);

        when(usuarioGateWay.obtenerUsuarioPorEmail(normalizedEmail)).thenReturn(null);
        when(passwordEncoderGateway.encode(rawPassword)).thenReturn(encodedPassword);
        when(usuarioGateWay.guardarUsuario(any(Usuario.class))).thenReturn(savedUser);
        when(tokenGateway.generateToken(userId, normalizedEmail, fullName)).thenReturn("jwt-token-123");

        // Act
        AuthResult result = authUseCase.register(rawEmail, rawPassword, fullName);

        // Assert
        assertNotNull(result);
        assertEquals("jwt-token-123", result.getToken());
        assertEquals(normalizedEmail, result.getUsuario().getEmail());
        verify(notificationGateway).notifyUserRegistration(normalizedEmail);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenRegisterEmailIsNull() {
        // Arrange
        String email = null;

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authUseCase.register(email, "password123", "Test User")
        );

        // Assert
        assertEquals("El email no puede estar vacío", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenRegisterEmailIsBlank() {
        // Arrange
        String email = "   ";

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authUseCase.register(email, "password123", "Test User")
        );

        // Assert
        assertEquals("El email no puede estar vacío", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenRegisterPasswordIsNull() {
        // Arrange
        String password = null;

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authUseCase.register("test@example.com", password, "Test User")
        );

        // Assert
        assertEquals("La contraseña debe tener al menos 6 caracteres", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenRegisterPasswordIsTooShort() {
        // Arrange
        String password = "12345";

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authUseCase.register("test@example.com", password, "Test User")
        );

        // Assert
        assertEquals("La contraseña debe tener al menos 6 caracteres", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenRegisterEmailAlreadyExists() {
        // Arrange
        String email = "existing@example.com";
        Usuario existingUser = new Usuario(UUID.randomUUID(), email, "hash", "Existing", null, "USD", true);
        when(usuarioGateWay.obtenerUsuarioPorEmail(email)).thenReturn(existingUser);

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authUseCase.register(email, "password123", "Existing")
        );

        // Assert
        assertEquals("Ya existe una cuenta registrada con este correo electrónico", exception.getMessage());
    }

    @Test
    void shouldCompleteRegistrationWhenNotificationGatewayThrowsException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String email = "resilient@example.com";
        Usuario savedUser = new Usuario(userId, email, "encoded", "Resilient User", null, "USD", true);

        when(usuarioGateWay.obtenerUsuarioPorEmail(email)).thenReturn(null);
        when(passwordEncoderGateway.encode("password123")).thenReturn("encoded");
        when(usuarioGateWay.guardarUsuario(any(Usuario.class))).thenReturn(savedUser);
        doThrow(new RuntimeException("Notification service down")).when(notificationGateway).notifyUserRegistration(email);
        when(tokenGateway.generateToken(userId, email, "Resilient User")).thenReturn("jwt-resilient");

        // Act
        AuthResult result = authUseCase.register(email, "password123", "Resilient User");

        // Assert
        assertNotNull(result);
        assertEquals("jwt-resilient", result.getToken());
        verify(notificationGateway).notifyUserRegistration(email);
    }

    @Test
    void shouldLoginSuccessfullyWhenCredentialsAreValid() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String email = "login@example.com";
        Usuario usuario = new Usuario(userId, email, "hashed_pwd", "Login User", null, "USD", true);

        when(usuarioGateWay.obtenerUsuarioPorEmail(email)).thenReturn(usuario);
        when(passwordEncoderGateway.matches("raw_pwd", "hashed_pwd")).thenReturn(true);
        when(tokenGateway.generateToken(userId, email, "Login User")).thenReturn("jwt-login-token");

        // Act
        AuthResult result = authUseCase.login("  LOGIN@example.com ", "raw_pwd");

        // Assert
        assertNotNull(result);
        assertEquals("jwt-login-token", result.getToken());
        assertEquals(email, result.getUsuario().getEmail());
        verify(notificationGateway).notifyLoginAlert(email);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenLoginEmailOrPasswordIsBlank() {
        // Arrange
        String blankEmail = " ";

        // Act
        IllegalArgumentException ex1 = assertThrows(
                IllegalArgumentException.class,
                () -> authUseCase.login(null, "pwd")
        );
        IllegalArgumentException ex2 = assertThrows(
                IllegalArgumentException.class,
                () -> authUseCase.login(blankEmail, "pwd")
        );
        IllegalArgumentException ex3 = assertThrows(
                IllegalArgumentException.class,
                () -> authUseCase.login("a@b.com", null)
        );
        IllegalArgumentException ex4 = assertThrows(
                IllegalArgumentException.class,
                () -> authUseCase.login("a@b.com", "  ")
        );

        // Assert
        assertEquals("El email y la contraseña son obligatorios", ex1.getMessage());
        assertEquals("El email y la contraseña son obligatorios", ex2.getMessage());
        assertEquals("El email y la contraseña son obligatorios", ex3.getMessage());
        assertEquals("El email y la contraseña son obligatorios", ex4.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenLoginUserNotFound() {
        // Arrange
        when(usuarioGateWay.obtenerUsuarioPorEmail("unknown@example.com")).thenReturn(null);

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authUseCase.login("unknown@example.com", "password123")
        );

        // Assert
        assertEquals("Credenciales inválidas", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenLoginAccountIsInactive() {
        // Arrange
        Usuario inactiveUser = new Usuario(UUID.randomUUID(), "inactive@example.com", "hash", "Inactive", null, "USD", false);
        when(usuarioGateWay.obtenerUsuarioPorEmail("inactive@example.com")).thenReturn(inactiveUser);

        // Act
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authUseCase.login("inactive@example.com", "password123")
        );

        // Assert
        assertEquals("La cuenta de usuario está desactivada", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenLoginPasswordDoesNotMatch() {
        // Arrange
        Usuario usuario = new Usuario(UUID.randomUUID(), "user@example.com", "correct_hash", "User", null, "USD", true);
        when(usuarioGateWay.obtenerUsuarioPorEmail("user@example.com")).thenReturn(usuario);
        when(passwordEncoderGateway.matches("wrong_pwd", "correct_hash")).thenReturn(false);

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authUseCase.login("user@example.com", "wrong_pwd")
        );

        // Assert
        assertEquals("Credenciales inválidas", exception.getMessage());
    }

    @Test
    void shouldCompleteLoginWhenNotifyLoginAlertThrowsException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String email = "alert@example.com";
        Usuario usuario = new Usuario(userId, email, "hash", "Alert User", null, "USD", true);

        when(usuarioGateWay.obtenerUsuarioPorEmail(email)).thenReturn(usuario);
        when(passwordEncoderGateway.matches("pwd123", "hash")).thenReturn(true);
        when(tokenGateway.generateToken(userId, email, "Alert User")).thenReturn("jwt-alert");
        doThrow(new RuntimeException("Feign error")).when(notificationGateway).notifyLoginAlert(email);

        // Act
        AuthResult result = authUseCase.login(email, "pwd123");

        // Assert
        assertNotNull(result);
        assertEquals("jwt-alert", result.getToken());
        verify(notificationGateway).notifyLoginAlert(email);
    }

    @Test
    void shouldChangePasswordSuccessfullyWhenInputsAreValid() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String email = "change@example.com";
        Usuario usuario = new Usuario(userId, email, "old_hash", "Change User", null, "USD", true);

        when(usuarioGateWay.obtenerUsuarioPorEmail(email)).thenReturn(usuario);
        when(passwordEncoderGateway.matches("oldPass123", "old_hash")).thenReturn(true);
        when(passwordEncoderGateway.encode("newPass1234")).thenReturn("new_hash");

        // Act
        authUseCase.changePassword("  CHANGE@example.com ", "oldPass123", "newPass1234");

        // Assert
        assertEquals("new_hash", usuario.getPasswordHash());
        verify(usuarioGateWay).actualizarUsuario(usuario);
        verify(notificationGateway).notifyPasswordChanged(email);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenChangePasswordFieldsAreNullOrBlank() {
        // Arrange
        String validEmail = "user@example.com";

        // Act
        IllegalArgumentException ex1 = assertThrows(
                IllegalArgumentException.class,
                () -> authUseCase.changePassword(null, "oldPass", "newPass123")
        );
        IllegalArgumentException ex2 = assertThrows(
                IllegalArgumentException.class,
                () -> authUseCase.changePassword(" ", "oldPass", "newPass123")
        );
        IllegalArgumentException ex3 = assertThrows(
                IllegalArgumentException.class,
                () -> authUseCase.changePassword(validEmail, null, "newPass123")
        );
        IllegalArgumentException ex4 = assertThrows(
                IllegalArgumentException.class,
                () -> authUseCase.changePassword(validEmail, " ", "newPass123")
        );
        IllegalArgumentException ex5 = assertThrows(
                IllegalArgumentException.class,
                () -> authUseCase.changePassword(validEmail, "oldPass", null)
        );
        IllegalArgumentException ex6 = assertThrows(
                IllegalArgumentException.class,
                () -> authUseCase.changePassword(validEmail, "oldPass", " ")
        );

        // Assert
        assertEquals("El email, la contraseña actual y la nueva contraseña no pueden ser nulos o vacíos", ex1.getMessage());
        assertEquals("El email, la contraseña actual y la nueva contraseña no pueden ser nulos o vacíos", ex2.getMessage());
        assertEquals("El email, la contraseña actual y la nueva contraseña no pueden ser nulos o vacíos", ex3.getMessage());
        assertEquals("El email, la contraseña actual y la nueva contraseña no pueden ser nulos o vacíos", ex4.getMessage());
        assertEquals("El email, la contraseña actual y la nueva contraseña no pueden ser nulos o vacíos", ex5.getMessage());
        assertEquals("El email, la contraseña actual y la nueva contraseña no pueden ser nulos o vacíos", ex6.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenNewPasswordIsShorterThanEightChars() {
        // Arrange
        String shortNewPassword = "1234567";

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authUseCase.changePassword("user@example.com", "oldPass123", shortNewPassword)
        );

        // Assert
        assertEquals("La nueva contraseña debe tener al menos 8 caracteres", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenNewPasswordEqualsCurrentPassword() {
        // Arrange
        String samePassword = "samePassword123";

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authUseCase.changePassword("user@example.com", samePassword, samePassword)
        );

        // Assert
        assertEquals("La nueva contraseña no puede ser igual a la contraseña actual", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenChangePasswordUserNotFound() {
        // Arrange
        when(usuarioGateWay.obtenerUsuarioPorEmail("missing@example.com")).thenReturn(null);

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authUseCase.changePassword("missing@example.com", "oldPass123", "newPass1234")
        );

        // Assert
        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenChangePasswordUserIsInactive() {
        // Arrange
        Usuario inactiveUser = new Usuario(UUID.randomUUID(), "inactive@example.com", "hash", "Inactive", null, "USD", false);
        when(usuarioGateWay.obtenerUsuarioPorEmail("inactive@example.com")).thenReturn(inactiveUser);

        // Act
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authUseCase.changePassword("inactive@example.com", "oldPass123", "newPass1234")
        );

        // Assert
        assertEquals("La cuenta de usuario está desactivada", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenChangePasswordCurrentPasswordIsIncorrect() {
        // Arrange
        Usuario usuario = new Usuario(UUID.randomUUID(), "user@example.com", "real_hash", "User", null, "USD", true);
        when(usuarioGateWay.obtenerUsuarioPorEmail("user@example.com")).thenReturn(usuario);
        when(passwordEncoderGateway.matches(eq("wrongCurrent"), eq("real_hash"))).thenReturn(false);

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authUseCase.changePassword("user@example.com", "wrongCurrent", "newPass1234")
        );

        // Assert
        assertEquals("La contraseña actual es incorrecta", exception.getMessage());
    }

    @Test
    void shouldCompleteChangePasswordWhenNotificationGatewayThrowsException() {
        // Arrange
        String email = "notifyfail@example.com";
        Usuario usuario = new Usuario(UUID.randomUUID(), email, "old_hash", "User", null, "USD", true);

        when(usuarioGateWay.obtenerUsuarioPorEmail(email)).thenReturn(usuario);
        when(passwordEncoderGateway.matches("oldPass123", "old_hash")).thenReturn(true);
        when(passwordEncoderGateway.encode("newPass1234")).thenReturn("new_hash");
        doThrow(new RuntimeException("Timeout")).when(notificationGateway).notifyPasswordChanged(email);

        // Act
        authUseCase.changePassword(email, "oldPass123", "newPass1234");

        // Assert
        assertEquals("new_hash", usuario.getPasswordHash());
        verify(usuarioGateWay).actualizarUsuario(usuario);
        verify(notificationGateway).notifyPasswordChanged(email);
    }
}
