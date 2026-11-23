package com.finance.account.infraestructure.driver_adapters.jpa_repository;

import com.finance.account.domain.model.Usuario;
import com.finance.account.infraestructure.mapper.UsuarioMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioEntityGatewayImplTest {

    @Mock
    private UsuarioEntityGatewayJpaRepository uJpaRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private UsuarioEntityGatewayImpl usuarioEntityGateway;

    @Test
    void shouldSaveUserSuccessfully() {
        // Arrange
        UUID id = UUID.randomUUID();
        Usuario domainUser = new Usuario(id, "test@example.com", "hash", "Test User", null, "USD", true);
        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(id);
        entity.setEmail("test@example.com");

        when(usuarioMapper.toEntity(domainUser)).thenReturn(entity);
        when(uJpaRepository.save(entity)).thenReturn(entity);
        when(usuarioMapper.toUser(entity)).thenReturn(domainUser);

        // Act
        Usuario result = usuarioEntityGateway.guardarUsuario(domainUser);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("test@example.com", result.getEmail());
        verify(uJpaRepository).save(entity);
    }

    @Test
    void shouldGetUserByIdWhenFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(id);
        Usuario domainUser = new Usuario(id, "found@example.com", "hash", "Found", null, "USD", true);

        when(uJpaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(usuarioMapper.toUser(entity)).thenReturn(domainUser);

        // Act
        Usuario result = usuarioEntityGateway.obtenerUsuarioPorId(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void shouldReturnNullWhenGetUserByIdNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(uJpaRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Usuario result = usuarioEntityGateway.obtenerUsuarioPorId(id);

        // Assert
        assertNull(result);
    }

    @Test
    void shouldGetUserByEmailWhenFound() {
        // Arrange
        String email = "found@example.com";
        UsuarioEntity entity = new UsuarioEntity();
        entity.setEmail(email);
        Usuario domainUser = new Usuario(UUID.randomUUID(), email, "hash", "Found", null, "USD", true);

        when(uJpaRepository.findByEmail(email)).thenReturn(Optional.of(entity));
        when(usuarioMapper.toUser(entity)).thenReturn(domainUser);

        // Act
        Usuario result = usuarioEntityGateway.obtenerUsuarioPorEmail(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @Test
    void shouldReturnNullWhenGetUserByEmailNotFound() {
        // Arrange
        String email = "missing@example.com";
        when(uJpaRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Usuario result = usuarioEntityGateway.obtenerUsuarioPorEmail(email);

        // Assert
        assertNull(result);
    }

    @Test
    void shouldUpdateUserWhenUserExists() {
        // Arrange
        UUID id = UUID.randomUUID();
        Usuario domainUser = new Usuario(id, "updated@example.com", "new_hash", "Updated Name", null, "COP", true);
        UsuarioEntity existingEntity = new UsuarioEntity();
        existingEntity.setId(id);
        existingEntity.setEmail("old@example.com");

        UsuarioEntity mappedEntity = new UsuarioEntity();
        mappedEntity.setId(id);
        mappedEntity.setEmail("updated@example.com");
        mappedEntity.setFullName("Updated Name");
        mappedEntity.setBaseCurrency("COP");

        when(uJpaRepository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(usuarioMapper.toEntity(domainUser)).thenReturn(mappedEntity);
        when(uJpaRepository.save(existingEntity)).thenReturn(existingEntity);
        when(usuarioMapper.toUser(existingEntity)).thenReturn(domainUser);

        // Act
        Usuario result = usuarioEntityGateway.actualizarUsuario(domainUser);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", result.getFullName());
        assertEquals("COP", result.getBaseCurrency());
        verify(uJpaRepository).save(existingEntity);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenUpdatingNonExistentUser() {
        // Arrange
        UUID id = UUID.randomUUID();
        Usuario domainUser = new Usuario(id, "missing@example.com", "hash", "Missing", null, "USD", true);
        when(uJpaRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioEntityGateway.actualizarUsuario(domainUser)
        );

        // Assert
        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(uJpaRepository, never()).save(any());
    }

    @Test
    void shouldDeleteUserByIdSuccessfully() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        usuarioEntityGateway.eliminarUsuarioPorId(id);

        // Assert
        verify(uJpaRepository).deleteById(id);
    }

    @Test
    void shouldMapBetweenEntityAndDomainUsingRealUsuarioMapper() {
        // Arrange
        UsuarioMapper realMapper = new UsuarioMapper();
        UUID id = UUID.randomUUID();
        Usuario domainUser = new Usuario(id, "map@example.com", "hash", "Map User", "http://avatar", "EUR", true);

        // Act
        UsuarioEntity mappedEntity = realMapper.toEntity(domainUser);
        Usuario mappedBackUser = realMapper.toUser(mappedEntity);
        UsuarioEntity nullEntity = realMapper.toEntity(null);
        Usuario nullUser = realMapper.toUser(null);

        // Assert
        assertNotNull(mappedEntity);
        assertEquals(id, mappedEntity.getId());
        assertNotNull(mappedBackUser);
        assertEquals("map@example.com", mappedBackUser.getEmail());
        assertEquals("EUR", mappedBackUser.getBaseCurrency());
        assertNull(nullEntity);
        assertNull(nullUser);
    }
}
