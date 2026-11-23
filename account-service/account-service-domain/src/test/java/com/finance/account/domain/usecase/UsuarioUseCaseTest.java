package com.finance.account.domain.usecase;

import com.finance.account.domain.gateway.NotificationGateway;
import com.finance.account.domain.model.Usuario;
import com.finance.account.domain.model.UsuarioGateWay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioUseCaseTest {

    @Mock
    private UsuarioGateWay usuarioGateWay;

    @Mock
    private NotificationGateway notificationGateway;

    private UsuarioUseCase usuarioUseCase;

    @BeforeEach
    void setUp() {
        usuarioUseCase = new UsuarioUseCase(usuarioGateWay, notificationGateway);
    }

    @Test
    void shouldGetUserByIdSuccessfullyWhenUserExists() {
        // Arrange
        UUID id = UUID.randomUUID();
        Usuario expected = new Usuario(id, "user@example.com", "hash", "User", null, "USD", true);
        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(expected);

        // Act
        Usuario actual = usuarioUseCase.obtenerUsuarioPorId(id);

        // Assert
        assertNotNull(actual);
        assertEquals(id, actual.getId());
        assertEquals("user@example.com", actual.getEmail());
    }

    @Test
    void shouldThrowNoSuchElementExceptionWhenGetUserByIdReturnsNull() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(null);

        // Act
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> usuarioUseCase.obtenerUsuarioPorId(id)
        );

        // Assert
        assertEquals("Usuario no puede ser null", exception.getMessage());
    }

    @Test
    void shouldGetUserByEmailSuccessfullyWhenUserExists() {
        // Arrange
        String email = "juan@example.com";
        Usuario expected = new Usuario(UUID.randomUUID(), email, "hash", "Juan", null, "USD", true);
        when(usuarioGateWay.obtenerUsuarioPorEmail(email)).thenReturn(expected);

        // Act
        Usuario actual = usuarioUseCase.obtenerUsuarioPorEmail(email);

        // Assert
        assertNotNull(actual);
        assertEquals(email, actual.getEmail());
    }

    @Test
    void shouldThrowNoSuchElementExceptionWhenGetUserByEmailReturnsNull() {
        // Arrange
        String email = "missing@example.com";
        when(usuarioGateWay.obtenerUsuarioPorEmail(email)).thenReturn(null);

        // Act
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> usuarioUseCase.obtenerUsuarioPorEmail(email)
        );

        // Assert
        assertEquals("Usuario no puede ser null", exception.getMessage());
    }

    @Test
    void shouldSaveUserSuccessfullyWhenValid() {
        // Arrange
        Usuario input = new Usuario(UUID.randomUUID(), "save@example.com", "hash", "Save User", null, "USD", true);
        when(usuarioGateWay.guardarUsuario(input)).thenReturn(input);

        // Act
        Usuario saved = usuarioUseCase.guardarUsuario(input);

        // Assert
        assertNotNull(saved);
        assertEquals("save@example.com", saved.getEmail());
        verify(usuarioGateWay).guardarUsuario(input);
    }

    @Test
    void shouldThrowNoSuchElementExceptionWhenSavingNullUser() {
        // Arrange
        Usuario nullUser = null;

        // Act
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> usuarioUseCase.guardarUsuario(nullUser)
        );

        // Assert
        assertEquals("Usuario no puede ser null", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenSavingUserWithNullOrBlankEmail() {
        // Arrange
        Usuario userNullEmail = new Usuario(UUID.randomUUID(), null, "hash", "User", null, "USD", true);
        Usuario userBlankEmail = new Usuario(UUID.randomUUID(), "   ", "hash", "User", null, "USD", true);

        // Act
        IllegalArgumentException ex1 = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(userNullEmail)
        );
        IllegalArgumentException ex2 = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(userBlankEmail)
        );

        // Assert
        assertEquals("Email no puede ser nulo o vacío.", ex1.getMessage());
        assertEquals("Email no puede ser nulo o vacío.", ex2.getMessage());
    }

    @Test
    void shouldUpdateUserSuccessfullyWhenValid() {
        // Arrange
        Usuario input = new Usuario(UUID.randomUUID(), "update@example.com", "hash", "Updated User", null, "USD", true);
        when(usuarioGateWay.actualizarUsuario(input)).thenReturn(input);

        // Act
        Usuario updated = usuarioUseCase.actualizarUsuario(input);

        // Assert
        assertNotNull(updated);
        assertEquals("Updated User", updated.getFullName());
        verify(usuarioGateWay).actualizarUsuario(input);
    }

    @Test
    void shouldDeleteUserByIdSuccessfullyWhenUserExists() {
        // Arrange
        UUID id = UUID.randomUUID();
        Usuario existing = new Usuario(id, "delete@example.com", "hash", "Delete User", null, "USD", true);
        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(existing);

        // Act
        usuarioUseCase.eliminarUsuarioPorId(id);

        // Assert
        verify(usuarioGateWay).eliminarUsuarioPorId(id);
    }

    @Test
    void shouldThrowNoSuchElementExceptionWhenDeletingNonExistentUser() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(null);

        // Act
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> usuarioUseCase.eliminarUsuarioPorId(id)
        );

        // Assert
        assertEquals("Usuario no puede ser null", exception.getMessage());
        verify(usuarioGateWay, never()).eliminarUsuarioPorId(id);
    }

    @Test
    void shouldDeactivateAccountAndNotifySuccessfully() {
        // Arrange
        UUID id = UUID.randomUUID();
        Usuario activeUser = new Usuario(id, "status@example.com", "hash", "Status User", null, "USD", true);
        Usuario deactivatedUser = new Usuario(id, "status@example.com", "hash", "Status User", null, "USD", false);

        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(activeUser);
        when(usuarioGateWay.actualizarUsuario(activeUser)).thenReturn(deactivatedUser);

        // Act
        Usuario result = usuarioUseCase.cambiarEstadoCuenta(id, false);

        // Assert
        assertFalse(result.getIsActive());
        verify(notificationGateway).notifyAccountStatusChanged("status@example.com", "DESACTIVADA");
    }

    @Test
    void shouldActivateAccountAndNotifySuccessfully() {
        // Arrange
        UUID id = UUID.randomUUID();
        Usuario inactiveUser = new Usuario(id, "status@example.com", "hash", "Status User", null, "USD", false);
        Usuario activatedUser = new Usuario(id, "status@example.com", "hash", "Status User", null, "USD", true);

        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(inactiveUser);
        when(usuarioGateWay.actualizarUsuario(inactiveUser)).thenReturn(activatedUser);

        // Act
        Usuario result = usuarioUseCase.cambiarEstadoCuenta(id, true);

        // Assert
        assertTrue(result.getIsActive());
        verify(notificationGateway).notifyAccountStatusChanged("status@example.com", "ACTIVADA");
    }

    @Test
    void shouldChangeAccountStatusWithoutNotificationWhenSingleArgConstructorUsed() {
        // Arrange
        UsuarioUseCase useCaseWithoutNotification = new UsuarioUseCase(usuarioGateWay);
        UUID id = UUID.randomUUID();
        Usuario activeUser = new Usuario(id, "single@example.com", "hash", "Single", null, "USD", true);

        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(activeUser);
        when(usuarioGateWay.actualizarUsuario(activeUser)).thenReturn(activeUser);

        // Act
        Usuario result = useCaseWithoutNotification.cambiarEstadoCuenta(id, false);

        // Assert
        assertFalse(result.getIsActive());
        verifyNoInteractions(notificationGateway);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenChangeAccountStatusParamsAreNull() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        IllegalArgumentException ex1 = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.cambiarEstadoCuenta(null, true)
        );
        IllegalArgumentException ex2 = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.cambiarEstadoCuenta(id, null)
        );

        // Assert
        assertEquals("El ID y el estado activo no pueden ser nulos", ex1.getMessage());
        assertEquals("El ID y el estado activo no pueden ser nulos", ex2.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenChangeAccountStatusUserNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(null);

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.cambiarEstadoCuenta(id, false)
        );

        // Assert
        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenChangeAccountStatusIsRedundant() {
        // Arrange
        UUID id = UUID.randomUUID();
        Usuario activeUser = new Usuario(id, "redundant@example.com", "hash", "Redundant", null, "USD", true);
        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(activeUser);

        // Act
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> usuarioUseCase.cambiarEstadoCuenta(id, true)
        );

        // Assert
        assertEquals("La cuenta ya se encuentra en el estado solicitado", exception.getMessage());
    }

    @Test
    void shouldCompleteChangeAccountStatusWhenNotificationGatewayThrowsException() {
        // Arrange
        UUID id = UUID.randomUUID();
        Usuario activeUser = new Usuario(id, "resilient@example.com", "hash", "Resilient", null, "USD", true);
        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(activeUser);
        when(usuarioGateWay.actualizarUsuario(activeUser)).thenReturn(activeUser);
        doThrow(new RuntimeException("Feign error")).when(notificationGateway)
                .notifyAccountStatusChanged("resilient@example.com", "DESACTIVADA");

        // Act
        Usuario result = usuarioUseCase.cambiarEstadoCuenta(id, false);

        // Assert
        assertFalse(result.getIsActive());
        verify(notificationGateway).notifyAccountStatusChanged("resilient@example.com", "DESACTIVADA");
    }

    @Test
    void shouldUpdateBaseCurrencyWithNormalizationSuccessfully() {
        // Arrange
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario(id, "currency@example.com", "hash", "Currency User", null, "USD", true);
        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(usuario);
        when(usuarioGateWay.actualizarUsuario(usuario)).thenReturn(usuario);

        // Act
        Usuario result = usuarioUseCase.actualizarMonedaBase(id, " cop ");

        // Assert
        assertEquals("COP", result.getBaseCurrency());
        verify(usuarioGateWay).actualizarUsuario(usuario);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenUpdateBaseCurrencyParamsAreNullOrBlank() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        IllegalArgumentException ex1 = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.actualizarMonedaBase(null, "USD")
        );
        IllegalArgumentException ex2 = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.actualizarMonedaBase(id, null)
        );
        IllegalArgumentException ex3 = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.actualizarMonedaBase(id, "   ")
        );

        // Assert
        assertEquals("El ID y la moneda base no pueden ser nulos o vacíos", ex1.getMessage());
        assertEquals("El ID y la moneda base no pueden ser nulos o vacíos", ex2.getMessage());
        assertEquals("El ID y la moneda base no pueden ser nulos o vacíos", ex3.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenBaseCurrencyIsUnsupported() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.actualizarMonedaBase(id, "GBP")
        );

        // Assert
        assertEquals("Moneda base no soportada", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenUpdateBaseCurrencyUserNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(null);

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.actualizarMonedaBase(id, "EUR")
        );

        // Assert
        assertEquals("Usuario no encontrado", exception.getMessage());
    }
}
