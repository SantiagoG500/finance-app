package com.finance.account.domain.usecase;

import com.finance.account.domain.gateway.NotificationGateway;
import com.finance.account.domain.gateway.PasswordEncoderGateway;
import com.finance.account.domain.gateway.TokenBlacklistGateway;
import com.finance.account.domain.gateway.TokenGateway;
import com.finance.account.domain.model.AuthResult;
import com.finance.account.domain.model.Usuario;
import com.finance.account.domain.model.UsuarioGateWay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Mock
    private TokenBlacklistGateway tokenBlacklistGateway;

    private AuthUseCase authUseCase;

    @BeforeEach
    void setUp() {
        authUseCase = new AuthUseCase(
                usuarioGateWay,
                passwordEncoderGateway,
                tokenGateway,
                notificationGateway,
                tokenBlacklistGateway
        );
    }

    @Test
    void register_ShouldSaveUserGenerateTokenAndNotify_WhenValidInput() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Usuario savedUser = new Usuario(userId, "test@finance.com", "hashed_pw", "Juan Perez", null, "USD", true);
        when(usuarioGateWay.obtenerUsuarioPorEmail("test@finance.com")).thenReturn(null);
        when(passwordEncoderGateway.encode("secret123")).thenReturn("hashed_pw");
        when(usuarioGateWay.guardarUsuario(any(Usuario.class))).thenReturn(savedUser);
        when(tokenGateway.generateToken(userId, "test@finance.com", "Juan Perez")).thenReturn("jwt-token-123");

        // Act
        AuthResult result = authUseCase.register("  TEST@finance.com ", "secret123", "Juan Perez");

        // Assert
        assertNotNull(result);
        assertEquals("jwt-token-123", result.getToken());
        assertEquals(userId, result.getUsuario().getId());
        assertEquals("test@finance.com", result.getUsuario().getEmail());
        verify(notificationGateway).notifyUserRegistration("test@finance.com");
    }

    @Test
    void register_ShouldSucceedEvenWhenNotificationGatewayThrowsOrIsNull() {
        // Arrange
        AuthUseCase useCaseWith4Args = new AuthUseCase(usuarioGateWay, passwordEncoderGateway, tokenGateway, notificationGateway);
        UUID userId = UUID.randomUUID();
        Usuario savedUser = new Usuario(userId, "user@finance.com", "hashed", "Ana Gomez", null, "USD", true);
        when(usuarioGateWay.obtenerUsuarioPorEmail("user@finance.com")).thenReturn(null);
        when(passwordEncoderGateway.encode("password123")).thenReturn("hashed");
        when(usuarioGateWay.guardarUsuario(any(Usuario.class))).thenReturn(savedUser);
        when(tokenGateway.generateToken(userId, "user@finance.com", "Ana Gomez")).thenReturn("jwt-usd");
        doThrow(new RuntimeException("Notification down")).when(notificationGateway).notifyUserRegistration("user@finance.com");

        // Act
        AuthResult result = useCaseWith4Args.register("user@finance.com", "password123", "Ana Gomez");

        // Assert
        assertEquals("jwt-usd", result.getToken());
        assertEquals("USD", result.getUsuario().getBaseCurrency());
    }

    @Test
    void register_ShouldThrowException_WhenEmailOrPasswordInvalid() {
        // Arrange
        String emptyEmail = "   ";
        String shortPass = "12345";

        // Act
        IllegalArgumentException exEmail = assertThrows(IllegalArgumentException.class,
                () -> authUseCase.register(emptyEmail, "secret123", "Juan"));
        IllegalArgumentException exPass = assertThrows(IllegalArgumentException.class,
                () -> authUseCase.register("test@finance.com", shortPass, "Juan"));

        // Assert
        assertEquals("El email no puede estar vacío", exEmail.getMessage());
        assertEquals("La contraseña debe tener al menos 6 caracteres", exPass.getMessage());
    }

    @Test
    void register_ShouldThrowIllegalStateException_WhenEmailAlreadyExists() {
        // Arrange
        Usuario existing = new Usuario(UUID.randomUUID(), "test@finance.com", "hash", "Juan", null, "USD", true);
        when(usuarioGateWay.obtenerUsuarioPorEmail("test@finance.com")).thenReturn(existing);

        // Act
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> authUseCase.register("test@finance.com", "secret123", "Juan"));

        // Assert
        assertEquals("Ya existe una cuenta registrada con este correo electrónico", ex.getMessage());
        verify(usuarioGateWay, never()).guardarUsuario(any());
    }

    @Test
    void login_ShouldReturnAuthResultRegisterActiveSessionAndNotify_WhenCredentialsAreValid() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Instant expiresAt = Instant.now().plusSeconds(3600);
        Usuario user = new Usuario(userId, "test@finance.com", "hashed_pw", "Juan Perez", null, "COP", true);
        when(usuarioGateWay.obtenerUsuarioPorEmail("test@finance.com")).thenReturn(user);
        when(passwordEncoderGateway.matches("secret123", "hashed_pw")).thenReturn(true);
        when(tokenBlacklistGateway.hasActiveSession("test@finance.com")).thenReturn(false);
        when(tokenGateway.generateToken(userId, "test@finance.com", "Juan Perez")).thenReturn("jwt-login");
        when(tokenGateway.extractExpiration("jwt-login")).thenReturn(expiresAt);

        // Act
        AuthResult result = authUseCase.login(" TEST@finance.com ", "secret123");

        // Assert
        assertNotNull(result);
        assertEquals("jwt-login", result.getToken());
        assertEquals(userId, result.getUsuario().getId());
        verify(tokenBlacklistGateway).registerActiveSession("test@finance.com", "jwt-login", expiresAt);
        verify(notificationGateway).notifyLoginAlert("test@finance.com");
    }

    @Test
    void login_ShouldThrowIllegalStateException_WhenUserAlreadyHasActiveSession() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Usuario user = new Usuario(userId, "test@finance.com", "hashed_pw", "Juan Perez", null, "COP", true);
        when(usuarioGateWay.obtenerUsuarioPorEmail("test@finance.com")).thenReturn(user);
        when(passwordEncoderGateway.matches("secret123", "hashed_pw")).thenReturn(true);
        when(tokenBlacklistGateway.hasActiveSession("test@finance.com")).thenReturn(true);

        // Act
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> authUseCase.login("test@finance.com", "secret123"));

        // Assert
        assertEquals("El usuario ya tiene una sesión activa. Debe cerrar sesión antes de volver a iniciar.", ex.getMessage());
        verify(tokenGateway, never()).generateToken(any(), any(), any());
        verify(tokenBlacklistGateway, never()).registerActiveSession(any(), any(), any());
    }

    @Test
    void login_ShouldAllowSecondLoginWithNewToken_AfterLogoutClearsActiveSession() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Instant firstExpiry = Instant.now().plusSeconds(3600);
        Instant secondExpiry = Instant.now().plusSeconds(7200);
        Usuario user = new Usuario(userId, "test@finance.com", "hashed_pw", "Juan Perez", null, "COP", true);
        when(usuarioGateWay.obtenerUsuarioPorEmail("test@finance.com")).thenReturn(user);
        when(passwordEncoderGateway.matches("secret123", "hashed_pw")).thenReturn(true);
        when(tokenBlacklistGateway.hasActiveSession("test@finance.com")).thenReturn(false, false);
        when(tokenGateway.generateToken(userId, "test@finance.com", "Juan Perez")).thenReturn("jwt-token-1", "jwt-token-2");
        when(tokenGateway.extractExpiration("jwt-token-1")).thenReturn(firstExpiry);
        when(tokenGateway.validateToken("jwt-token-1")).thenReturn(true);
        when(tokenGateway.extractExpiration("jwt-token-2")).thenReturn(secondExpiry);

        // Act
        AuthResult firstLogin = authUseCase.login("test@finance.com", "secret123");
        authUseCase.logout("Bearer jwt-token-1");
        AuthResult secondLogin = authUseCase.login("test@finance.com", "secret123");

        // Assert
        assertEquals("jwt-token-1", firstLogin.getToken());
        assertEquals("jwt-token-2", secondLogin.getToken());
        verify(tokenBlacklistGateway).registerActiveSession("test@finance.com", "jwt-token-1", firstExpiry);
        verify(tokenBlacklistGateway).revokeToken("jwt-token-1", firstExpiry);
        verify(tokenBlacklistGateway).clearActiveSessionByToken("jwt-token-1");
        verify(tokenBlacklistGateway).registerActiveSession("test@finance.com", "jwt-token-2", secondExpiry);
    }

    @Test
    void login_ShouldThrowException_WhenCredentialsInvalidOrUserNotFoundOrInactive() {
        // Arrange
        Usuario inactiveUser = new Usuario(UUID.randomUUID(), "inactive@finance.com", "hash", "Juan", null, "COP", false);
        Usuario activeUser = new Usuario(UUID.randomUUID(), "active@finance.com", "hash", "Juan", null, "COP", true);
        when(usuarioGateWay.obtenerUsuarioPorEmail("missing@finance.com")).thenReturn(null);
        when(usuarioGateWay.obtenerUsuarioPorEmail("inactive@finance.com")).thenReturn(inactiveUser);
        when(usuarioGateWay.obtenerUsuarioPorEmail("active@finance.com")).thenReturn(activeUser);
        when(passwordEncoderGateway.matches("wrongpass", "hash")).thenReturn(false);

        // Act
        IllegalArgumentException exEmpty = assertThrows(IllegalArgumentException.class,
                () -> authUseCase.login("", "secret123"));
        IllegalArgumentException exNotFound = assertThrows(IllegalArgumentException.class,
                () -> authUseCase.login("missing@finance.com", "secret123"));
        IllegalStateException exInactive = assertThrows(IllegalStateException.class,
                () -> authUseCase.login("inactive@finance.com", "secret123"));
        IllegalArgumentException exWrongPass = assertThrows(IllegalArgumentException.class,
                () -> authUseCase.login("active@finance.com", "wrongpass"));

        // Assert
        assertEquals("El email y la contraseña son obligatorios", exEmpty.getMessage());
        assertEquals("Credenciales inválidas", exNotFound.getMessage());
        assertEquals("La cuenta de usuario está desactivada", exInactive.getMessage());
        assertEquals("Credenciales inválidas", exWrongPass.getMessage());
    }

    @Test
    void changePassword_ShouldUpdateHashAndNotify_WhenValidRequest() {
        // Arrange
        Usuario user = new Usuario(UUID.randomUUID(), "test@finance.com", "old_hash", "Juan Perez", null, "COP", true);
        when(usuarioGateWay.obtenerUsuarioPorEmail("test@finance.com")).thenReturn(user);
        when(passwordEncoderGateway.matches("oldPass123", "old_hash")).thenReturn(true);
        when(passwordEncoderGateway.encode("newPass45678")).thenReturn("new_hash");

        // Act
        authUseCase.changePassword("test@finance.com", "oldPass123", "newPass45678");

        // Assert
        assertEquals("new_hash", user.getPasswordHash());
        verify(usuarioGateWay).actualizarUsuario(user);
        verify(notificationGateway).notifyPasswordChanged("test@finance.com");
    }

    @Test
    void changePassword_ShouldThrowException_WhenValidationsOrLookupFail() {
        // Arrange
        String samePass = "samePass123";
        Usuario inactiveUser = new Usuario(UUID.randomUUID(), "inactive@finance.com", "hash", "Juan", null, "COP", false);
        Usuario activeUser = new Usuario(UUID.randomUUID(), "active@finance.com", "hash", "Juan", null, "COP", true);
        when(usuarioGateWay.obtenerUsuarioPorEmail("missing@finance.com")).thenReturn(null);
        when(usuarioGateWay.obtenerUsuarioPorEmail("inactive@finance.com")).thenReturn(inactiveUser);
        when(usuarioGateWay.obtenerUsuarioPorEmail("active@finance.com")).thenReturn(activeUser);
        when(passwordEncoderGateway.matches("wrongCurrent", "hash")).thenReturn(false);

        // Act
        IllegalArgumentException exEmptyEmail = assertThrows(IllegalArgumentException.class,
                () -> authUseCase.changePassword(" ", "oldPass123", "newPass1234"));
        IllegalArgumentException exShortNew = assertThrows(IllegalArgumentException.class,
                () -> authUseCase.changePassword("a@b.com", "oldPass123", "1234567"));
        IllegalArgumentException exSamePass = assertThrows(IllegalArgumentException.class,
                () -> authUseCase.changePassword("a@b.com", samePass, samePass));
        NoSuchElementException exNotFound = assertThrows(NoSuchElementException.class,
                () -> authUseCase.changePassword("missing@finance.com", "old12345", "new123456"));
        IllegalStateException exInactive = assertThrows(IllegalStateException.class,
                () -> authUseCase.changePassword("inactive@finance.com", "old12345", "new123456"));
        IllegalArgumentException exWrongPass = assertThrows(IllegalArgumentException.class,
                () -> authUseCase.changePassword("active@finance.com", "wrongCurrent", "new123456"));

        // Assert
        assertEquals("El email, la contraseña actual y la nueva contraseña no pueden ser nulos o vacíos", exEmptyEmail.getMessage());
        assertEquals("La nueva contraseña debe tener al menos 8 caracteres", exShortNew.getMessage());
        assertEquals("La nueva contraseña no puede ser igual a la contraseña actual", exSamePass.getMessage());
        assertEquals("Usuario no encontrado", exNotFound.getMessage());
        assertEquals("La cuenta de usuario está desactivada", exInactive.getMessage());
        assertEquals("La contraseña actual es incorrecta", exWrongPass.getMessage());
    }

    @Test
    void logout_ShouldRevokeTokenAndClearActiveSession_WhenBearerHeaderIsValid() {
        // Arrange
        String token = "valid.jwt.token";
        Instant expiresAt = Instant.now().plusSeconds(3600);
        when(tokenGateway.validateToken(token)).thenReturn(true);
        when(tokenGateway.extractExpiration(token)).thenReturn(expiresAt);

        // Act
        authUseCase.logout("Bearer " + token);

        // Assert
        verify(tokenGateway).validateToken(token);
        verify(tokenGateway).extractExpiration(token);
        verify(tokenBlacklistGateway).revokeToken(token, expiresAt);
        verify(tokenBlacklistGateway).clearActiveSessionByToken(token);
    }

    @Test
    void logout_ShouldThrowException_WhenHeaderIsNullOrBlankOrInvalidToken() {
        // Arrange
        when(tokenGateway.validateToken("invalid-token")).thenReturn(false);

        // Act
        IllegalArgumentException exNull = assertThrows(IllegalArgumentException.class,
                () -> authUseCase.logout(null));
        IllegalArgumentException exBlank = assertThrows(IllegalArgumentException.class,
                () -> authUseCase.logout("   "));
        IllegalArgumentException exEmptyBearer = assertThrows(IllegalArgumentException.class,
                () -> authUseCase.logout("Bearer    "));
        IllegalArgumentException exInvalid = assertThrows(IllegalArgumentException.class,
                () -> authUseCase.logout("invalid-token"));

        // Assert
        assertEquals("El token de autorización es obligatorio", exNull.getMessage());
        assertEquals("El token de autorización es obligatorio", exBlank.getMessage());
        assertEquals("Token JWT inválido o expirado", exEmptyBearer.getMessage());
        assertEquals("Token JWT inválido o expirado", exInvalid.getMessage());
        verify(tokenBlacklistGateway, never()).revokeToken(any(), any());
    }
}
