package com.finance.account.domain.usecase;

import com.finance.account.domain.gateway.NotificationGateway;
import com.finance.account.domain.gateway.PasswordEncoderGateway;
import com.finance.account.domain.model.Usuario;
import com.finance.account.domain.model.UsuarioGateWay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioUseCaseTest {

    @Mock
    private UsuarioGateWay usuarioGateWay;

    @Mock
    private NotificationGateway notificationGateway;

    @Mock
    private PasswordEncoderGateway passwordEncoderGateway;

    private UsuarioUseCase usuarioUseCase;

    @BeforeEach
    void setUp() {
        usuarioUseCase = new UsuarioUseCase(usuarioGateWay, notificationGateway, passwordEncoderGateway);
    }

    @Test
    void guardarUsuario_ShouldSaveAndReturnUser_WhenEmailIsValid() {
        // Arrange
        Usuario user = new Usuario(UUID.randomUUID(), "juan@finance.com", "hash", "Juan Perez", null, "COP", true);
        when(usuarioGateWay.guardarUsuario(user)).thenReturn(user);

        // Act
        Usuario result = usuarioUseCase.guardarUsuario(user);

        // Assert
        assertNotNull(result);
        assertEquals("juan@finance.com", result.getEmail());
        verify(usuarioGateWay).guardarUsuario(user);
    }

    @Test
    void guardarUsuario_ShouldThrowException_WhenUserIsNullOrEmailIsEmpty() {
        // Arrange
        Usuario userWithoutEmail = new Usuario(UUID.randomUUID(), "  ", "hash", "Juan Perez", null, "COP", true);

        // Act
        NoSuchElementException exNull = assertThrows(NoSuchElementException.class,
                () -> usuarioUseCase.guardarUsuario(null));
        IllegalArgumentException exEmptyEmail = assertThrows(IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(userWithoutEmail));

        // Assert
        assertEquals("Usuario no puede ser null", exNull.getMessage());
        assertEquals("Email no puede ser nulo o vacío.", exEmptyEmail.getMessage());
    }

    @Test
    void eliminarUsuarioPorId_ShouldDelete_WhenUserExists() {
        // Arrange
        UUID id = UUID.randomUUID();
        Usuario existing = new Usuario(id, "juan@finance.com", "hash", "Juan", null, "COP", true);
        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(existing);

        // Act
        usuarioUseCase.eliminarUsuarioPorId(id);

        // Assert
        verify(usuarioGateWay).eliminarUsuarioPorId(id);
    }

    @Test
    void eliminarUsuarioPorId_ShouldThrowNoSuchElementException_WhenUserNotFound() {
        // Arrange
        UUID missingId = UUID.randomUUID();
        when(usuarioGateWay.obtenerUsuarioPorId(missingId)).thenReturn(null);

        // Act
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> usuarioUseCase.eliminarUsuarioPorId(missingId));

        // Assert
        assertEquals("Usuario no puede ser null", exception.getMessage());
        verify(usuarioGateWay, never()).eliminarUsuarioPorId(missingId);
    }

    @Test
    void obtenerUsuarioPorIdAndEmail_ShouldReturnUser_WhenFound() {
        // Arrange
        UsuarioUseCase singleArgUseCase = new UsuarioUseCase(usuarioGateWay);
        UsuarioUseCase twoArgUseCase = new UsuarioUseCase(usuarioGateWay, notificationGateway);
        UUID id = UUID.randomUUID();
        Usuario existing = new Usuario(id, "juan@finance.com", "hash", "Juan", null, "COP", true);
        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(existing);
        when(usuarioGateWay.obtenerUsuarioPorEmail("juan@finance.com")).thenReturn(existing);

        // Act
        Usuario byId = singleArgUseCase.obtenerUsuarioPorId(id);
        Usuario byEmail = twoArgUseCase.obtenerUsuarioPorEmail("juan@finance.com");

        // Assert
        assertEquals(id, byId.getId());
        assertEquals("juan@finance.com", byEmail.getEmail());
    }

    @Test
    void actualizarUsuario_ShouldUpdateAllFieldsAndEncodePassword_WhenFullInputIsValid() {
        // Arrange
        UUID id = UUID.randomUUID();
        Usuario existing = new Usuario(id, "old@finance.com", "old_hash", "Old Name", "https://old.png", "COP", true);
        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(existing);
        when(usuarioGateWay.obtenerUsuarioPorEmail("new@finance.com")).thenReturn(null);
        when(passwordEncoderGateway.encode("newSecret123")).thenReturn("encoded_new_secret");
        when(usuarioGateWay.actualizarUsuario(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Usuario result = usuarioUseCase.actualizarUsuario(
                id,
                " NEW@finance.com ",
                "newSecret123",
                " New Name ",
                " https://new.png ",
                "usd",
                false
        );

        // Assert
        assertEquals("new@finance.com", result.getEmail());
        assertEquals("encoded_new_secret", result.getPasswordHash());
        assertEquals("New Name", result.getFullName());
        assertEquals("https://new.png", result.getAvatarUrl());
        assertEquals("USD", result.getBaseCurrency());
        assertFalse(result.getIsActive());
    }

    @Test
    void actualizarUsuario_ShouldPreservePasswordAndActiveStatus_WhenOptionalFieldsAreNullOrBlank() {
        // Arrange
        UUID id = UUID.randomUUID();
        Usuario existing = new Usuario(id, "juan@finance.com", "preserved_bcrypt_hash", "Juan", "https://old.png", "COP", true);
        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(existing);
        when(usuarioGateWay.obtenerUsuarioPorEmail("juan@finance.com")).thenReturn(existing);
        when(usuarioGateWay.actualizarUsuario(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Usuario resultNullOptional = usuarioUseCase.actualizarUsuario(
                id,
                "juan@finance.com",
                null,
                "Juan Actualizado",
                "https://avatar.png",
                "EUR",
                null
        );
        Usuario resultBlankPassword = usuarioUseCase.actualizarUsuario(
                id,
                "juan@finance.com",
                "   ",
                "Juan Perfil",
                "https://avatar2.png",
                "MXN",
                null
        );

        // Assert
        assertEquals("preserved_bcrypt_hash", resultNullOptional.getPasswordHash());
        assertTrue(resultNullOptional.getIsActive());
        assertEquals("preserved_bcrypt_hash", resultBlankPassword.getPasswordHash());
        assertEquals("MXN", resultBlankPassword.getBaseCurrency());
        assertTrue(resultBlankPassword.getIsActive());
        verifyNoInteractions(passwordEncoderGateway);
    }

    @Test
    void actualizarUsuario_ShouldThrowException_WhenUpdateValidationsOrLookupFail() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        Usuario existing = new Usuario(id, "juan@finance.com", "hash", "Juan", "https://a.png", "COP", true);
        Usuario otherUser = new Usuario(otherId, "taken@finance.com", "hash", "Other", "https://b.png", "COP", true);
        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(existing);
        when(usuarioGateWay.obtenerUsuarioPorId(missingId)).thenReturn(null);
        when(usuarioGateWay.obtenerUsuarioPorEmail("taken@finance.com")).thenReturn(otherUser);

        // Act
        IllegalArgumentException exMissingFields = assertThrows(IllegalArgumentException.class,
                () -> usuarioUseCase.actualizarUsuario(null, "a@b.com", null, "Name", "url", "COP", null));
        IllegalArgumentException exShortPass = assertThrows(IllegalArgumentException.class,
                () -> usuarioUseCase.actualizarUsuario(id, "a@b.com", "123", "Name", "url", "COP", null));
        IllegalArgumentException exUnsupportedCurrency = assertThrows(IllegalArgumentException.class,
                () -> usuarioUseCase.actualizarUsuario(id, "a@b.com", null, "Name", "url", "JPY", null));
        NoSuchElementException exNotFound = assertThrows(NoSuchElementException.class,
                () -> usuarioUseCase.actualizarUsuario(missingId, "a@b.com", null, "Name", "url", "COP", null));
        IllegalStateException exDuplicateEmail = assertThrows(IllegalStateException.class,
                () -> usuarioUseCase.actualizarUsuario(id, "taken@finance.com", null, "Name", "url", "COP", null));

        // Assert
        assertEquals("Todos los campos del usuario son obligatorios para la actualización completa", exMissingFields.getMessage());
        assertEquals("La contraseña debe tener al menos 6 caracteres", exShortPass.getMessage());
        assertEquals("Moneda base no soportada", exUnsupportedCurrency.getMessage());
        assertEquals("Usuario no encontrado", exNotFound.getMessage());
        assertEquals("Ya existe una cuenta registrada con este correo electrónico", exDuplicateEmail.getMessage());
    }

    @Test
    void actualizarUsuario_ShouldUpdateDomainObject_WhenValid() {
        // Arrange
        UUID id = UUID.randomUUID();
        Usuario updated = new Usuario(id, "juan@finance.com", "hash", "Juan Actualizado", "https://a.png", "USD", true);
        when(usuarioGateWay.actualizarUsuario(updated)).thenReturn(updated);

        // Act
        Usuario result = usuarioUseCase.actualizarUsuario(updated);

        // Assert
        assertEquals("Juan Actualizado", result.getFullName());
    }

    @Test
    void cambiarEstadoCuenta_ShouldDeactivateAndActivateAndNotify() {
        // Arrange
        UUID id = UUID.randomUUID();
        Usuario activeUser = new Usuario(id, "status@example.com", "hash", "Status User", null, "USD", true);
        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(activeUser);
        when(usuarioGateWay.actualizarUsuario(activeUser)).thenReturn(activeUser);

        // Act
        Usuario deactivated = usuarioUseCase.cambiarEstadoCuenta(id, false);
        Usuario activated = usuarioUseCase.cambiarEstadoCuenta(id, true);

        // Assert
        assertTrue(activated.getIsActive());
        assertNotNull(deactivated);
        verify(notificationGateway).notifyAccountStatusChanged("status@example.com", "DESACTIVADA");
        verify(notificationGateway).notifyAccountStatusChanged("status@example.com", "ACTIVADA");
    }

    @Test
    void cambiarEstadoCuenta_ShouldSucceedWhenNotificationIsNullOrThrows() {
        // Arrange
        UsuarioUseCase useCaseWithoutNotification = new UsuarioUseCase(usuarioGateWay);
        UUID id = UUID.randomUUID();
        Usuario activeUser = new Usuario(id, "resilient@example.com", "hash", "Resilient", null, "USD", true);
        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(activeUser);
        when(usuarioGateWay.actualizarUsuario(activeUser)).thenReturn(activeUser);
        doThrow(new RuntimeException("Feign error")).when(notificationGateway)
                .notifyAccountStatusChanged("resilient@example.com", "ACTIVADA");

        // Act
        Usuario r1 = useCaseWithoutNotification.cambiarEstadoCuenta(id, false);
        Usuario r2 = usuarioUseCase.cambiarEstadoCuenta(id, true);

        // Assert
        assertNotNull(r1);
        assertTrue(r2.getIsActive());
        verify(notificationGateway).notifyAccountStatusChanged("resilient@example.com", "ACTIVADA");
    }

    @Test
    void cambiarEstadoCuenta_ShouldThrowException_WhenInvalidOrNotFoundOrRedundant() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();
        Usuario activeUser = new Usuario(id, "redundant@example.com", "hash", "Redundant", null, "USD", true);
        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(activeUser);
        when(usuarioGateWay.obtenerUsuarioPorId(missingId)).thenReturn(null);

        // Act
        IllegalArgumentException exNullId = assertThrows(IllegalArgumentException.class,
                () -> usuarioUseCase.cambiarEstadoCuenta(null, true));
        NoSuchElementException exMissing = assertThrows(NoSuchElementException.class,
                () -> usuarioUseCase.cambiarEstadoCuenta(missingId, false));
        IllegalStateException exRedundant = assertThrows(IllegalStateException.class,
                () -> usuarioUseCase.cambiarEstadoCuenta(id, true));

        // Assert
        assertEquals("El ID y el estado activo no pueden ser nulos", exNullId.getMessage());
        assertEquals("Usuario no encontrado", exMissing.getMessage());
        assertEquals("La cuenta ya se encuentra en el estado solicitado", exRedundant.getMessage());
        verifyNoInteractions(notificationGateway);
    }

    @Test
    void actualizarMonedaBase_ShouldUpdateCurrency_WhenSupported() {
        // Arrange
        UUID id = UUID.randomUUID();
        Usuario user = new Usuario(id, "juan@finance.com", "hash", "Juan Perez", null, "COP", true);
        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(user);
        when(usuarioGateWay.actualizarUsuario(user)).thenReturn(user);

        // Act
        Usuario result = usuarioUseCase.actualizarMonedaBase(id, " eur ");

        // Assert
        assertEquals("EUR", result.getBaseCurrency());
    }

    @Test
    void actualizarMonedaBase_ShouldThrowException_WhenInvalidOrUnsupportedOrNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(usuarioGateWay.obtenerUsuarioPorId(id)).thenReturn(null);

        // Act
        IllegalArgumentException exNullId = assertThrows(IllegalArgumentException.class,
                () -> usuarioUseCase.actualizarMonedaBase(null, "USD"));
        IllegalArgumentException exBlank = assertThrows(IllegalArgumentException.class,
                () -> usuarioUseCase.actualizarMonedaBase(id, "   "));
        IllegalArgumentException exUnsupported = assertThrows(IllegalArgumentException.class,
                () -> usuarioUseCase.actualizarMonedaBase(id, "GBP"));
        NoSuchElementException exNotFound = assertThrows(NoSuchElementException.class,
                () -> usuarioUseCase.actualizarMonedaBase(id, "USD"));

        // Assert
        assertEquals("El ID y la moneda base no pueden ser nulos o vacíos", exNullId.getMessage());
        assertEquals("El ID y la moneda base no pueden ser nulos o vacíos", exBlank.getMessage());
        assertEquals("Moneda base no soportada", exUnsupported.getMessage());
        assertEquals("Usuario no encontrado", exNotFound.getMessage());
    }
}
