package com.finance.account.infraestructure.entry_points;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.account.domain.gateway.TokenBlacklistGateway;
import com.finance.account.domain.gateway.TokenGateway;
import com.finance.account.domain.model.Usuario;
import com.finance.account.domain.usecase.UsuarioUseCase;
import com.finance.account.infraestructure.config.AsyncConfig;
import com.finance.account.infraestructure.config.JwtAuthenticationFilter;
import com.finance.account.infraestructure.config.SecurityConfig;
import com.finance.account.infraestructure.entry_points.dto.UpdateAccountStatusRequestDTO;
import com.finance.account.infraestructure.entry_points.dto.UpdateBaseCurrencyRequestDTO;
import com.finance.account.infraestructure.entry_points.dto.UpdateUsuarioRequestDTO;
import com.finance.account.infraestructure.entry_points.exception.GlobalExceptionHandler;
import feign.Request;
import feign.RequestTemplate;
import feign.RetryableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UsuarioController.class)
@ContextConfiguration(classes = {
        UsuarioControllerTest.TestApp.class,
        UsuarioController.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        GlobalExceptionHandler.class
})
class UsuarioControllerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({UsuarioController.class, SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
    static class TestApp {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioUseCase usuarioUseCase;

    @MockBean
    private TokenGateway tokenGateway;

    @MockBean
    private TokenBlacklistGateway tokenBlacklistGateway;

    @Test
    void shouldReturn401UnauthorizedWhenRequestingProtectedEndpointWithoutJwt() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        ResultActions response = mockMvc.perform(get("/api/usuarios/get/{id}", id));

        // Assert
        response.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("No autorizado"));
    }

    @Test
    void shouldAuthenticateViaJwtFilterWhenValidNonRevokedBearerTokenProvided() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        String validToken = "valid.jwt.token";
        Usuario user = new Usuario(id, "jwt@example.com", "secret_hash", "Jwt User", "http://avatar", "USD", true, LocalDateTime.now());

        when(tokenGateway.validateToken(validToken)).thenReturn(true);
        when(tokenBlacklistGateway.isTokenRevoked(validToken)).thenReturn(false);
        when(tokenGateway.extractEmail(validToken)).thenReturn("jwt@example.com");
        when(usuarioUseCase.obtenerUsuarioPorId(id)).thenReturn(user);

        // Act
        ResultActions response = mockMvc.perform(get("/api/usuarios/get/{id}", id)
                .header("Authorization", "Bearer " + validToken));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("jwt@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void shouldRejectWith401WhenBearerTokenIsRevokedOrInvalid() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        String revokedToken = "revoked.jwt.token";
        when(tokenGateway.validateToken(revokedToken)).thenReturn(true);
        when(tokenBlacklistGateway.isTokenRevoked(revokedToken)).thenReturn(true);

        // Act
        ResultActions response = mockMvc.perform(get("/api/usuarios/get/{id}", id)
                .header("Authorization", "Bearer " + revokedToken));

        // Assert
        response.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(username = "get@example.com")
    void shouldReturn403ForbiddenWhenAuthenticatedUserDoesNotOwnTargetAccount() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        Usuario otherUser = new Usuario(id, "other@example.com", "hash", "Other User", "http://a", "USD", true);
        when(usuarioUseCase.obtenerUsuarioPorId(id)).thenReturn(otherUser);

        // Act
        ResultActions response = mockMvc.perform(get("/api/usuarios/get/{id}", id));

        // Assert
        response.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("No tiene permisos para modificar esta cuenta"));
    }

    @Test
    @WithMockUser(username = "upd@example.com")
    void shouldUpdateUserWithFullPutAndExcludePasswordHash() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        Usuario existingUser = new Usuario(id, "upd@example.com", "old_hash", "Old Name", "http://old", "COP", true);
        Usuario updatedUser = new Usuario(id, "upd@example.com", "new_hash", "Updated Name", "http://new", "USD", true);
        UpdateUsuarioRequestDTO request = new UpdateUsuarioRequestDTO(
                "upd@example.com",
                "newPassword123",
                "Updated Name",
                "http://new",
                "USD",
                true
        );

        when(usuarioUseCase.obtenerUsuarioPorId(id)).thenReturn(existingUser);
        when(usuarioUseCase.actualizarUsuario(id, "upd@example.com", "newPassword123", "Updated Name", "http://new", "USD", true))
                .thenReturn(updatedUser);

        // Act
        ResultActions response = mockMvc.perform(put("/api/usuarios/update/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.baseCurrency").value("USD"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @WithMockUser(username = "del@example.com")
    void shouldDeleteUserByIdWhenOwner() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        Usuario existingUser = new Usuario(id, "del@example.com", "hash", "Del User", "http://a", "COP", true);
        when(usuarioUseCase.obtenerUsuarioPorId(id)).thenReturn(existingUser);
        doNothing().when(usuarioUseCase).eliminarUsuarioPorId(id);

        // Act
        ResultActions response = mockMvc.perform(delete("/api/usuarios/delete/{id}", id));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuario eliminado con éxito"));
        verify(usuarioUseCase).eliminarUsuarioPorId(id);
    }

    @Test
    @WithMockUser(username = "curr@example.com")
    void shouldPatchBaseCurrencyWhenOwner() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        UpdateBaseCurrencyRequestDTO request = new UpdateBaseCurrencyRequestDTO("EUR");
        Usuario existingUser = new Usuario(id, "curr@example.com", "hash", "Curr User", null, "COP", true);
        Usuario updatedUser = new Usuario(id, "curr@example.com", "hash", "Curr User", null, "EUR", true);

        when(usuarioUseCase.obtenerUsuarioPorId(id)).thenReturn(existingUser);
        when(usuarioUseCase.actualizarMonedaBase(id, "EUR")).thenReturn(updatedUser);

        // Act
        ResultActions response = mockMvc.perform(patch("/api/usuarios/{id}/currency", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Divisa base se cambió a EUR"));
    }

    @Test
    @WithMockUser(username = "status@example.com")
    void shouldPatchAccountStatusWhenOwnerForSuspendAndReactivate() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        UpdateAccountStatusRequestDTO suspendReq = new UpdateAccountStatusRequestDTO(false);
        UpdateAccountStatusRequestDTO activateReq = new UpdateAccountStatusRequestDTO(true);
        Usuario existingUser = new Usuario(id, "status@example.com", "hash", "Status User", null, "USD", true);
        Usuario suspendedUser = new Usuario(id, "status@example.com", "hash", "Status User", null, "USD", false);
        Usuario reactivatedUser = new Usuario(id, "status@example.com", "hash", "Status User", null, "USD", true);

        when(usuarioUseCase.obtenerUsuarioPorId(id)).thenReturn(existingUser);
        when(usuarioUseCase.cambiarEstadoCuenta(id, false)).thenReturn(suspendedUser);
        when(usuarioUseCase.cambiarEstadoCuenta(id, true)).thenReturn(reactivatedUser);

        // Act
        ResultActions suspendResponse = mockMvc.perform(patch("/api/usuarios/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(suspendReq)));
        ResultActions activateResponse = mockMvc.perform(patch("/api/usuarios/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activateReq)));

        // Assert
        suspendResponse.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("La cuenta status@example.com ha sido suspendida"));
        activateResponse.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("La cuenta status@example.com ha sido reactivada"));
    }

    @Test
    @WithMockUser(username = "tester@example.com")
    void shouldHandleGlobalExceptionsForTypeMismatchMalformedJsonNotFoundConflictTimeoutSaturatedAndInternalError() throws Exception {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        UUID conflictId = UUID.randomUUID();
        UUID dataConflictId = UUID.randomUUID();
        UUID timeoutId = UUID.randomUUID();
        UUID rejectedId = UUID.randomUUID();
        UUID errorId = UUID.randomUUID();
        Request feignRequest = Request.create(Request.HttpMethod.POST, "/api/notifications", Collections.emptyMap(), null, new RequestTemplate());
        RetryableException retryableException = new RetryableException(504, "Read timed out", Request.HttpMethod.POST, new Date(), feignRequest);

        when(usuarioUseCase.obtenerUsuarioPorId(notFoundId)).thenThrow(new NoSuchElementException("No existe"));
        when(usuarioUseCase.obtenerUsuarioPorId(conflictId)).thenThrow(new IllegalStateException("Conflicto"));
        when(usuarioUseCase.obtenerUsuarioPorId(dataConflictId)).thenThrow(new DataIntegrityViolationException("Constraint"));
        when(usuarioUseCase.obtenerUsuarioPorId(timeoutId)).thenThrow(retryableException);
        when(usuarioUseCase.obtenerUsuarioPorId(rejectedId)).thenThrow(new TaskRejectedException("Queue full"));
        when(usuarioUseCase.obtenerUsuarioPorId(errorId)).thenThrow(new RuntimeException("Fallo inesperado"));

        // Act
        ResultActions typeMismatchRes = mockMvc.perform(get("/api/usuarios/get/not-a-uuid"));
        ResultActions malformedJsonRes = mockMvc.perform(patch("/api/usuarios/{id}/currency", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{malformed-json"));
        ResultActions methodNotAllowedRes = mockMvc.perform(delete("/api/usuarios/{id}/currency", UUID.randomUUID()));
        ResultActions notFoundRes = mockMvc.perform(get("/api/usuarios/get/{id}", notFoundId));
        ResultActions conflictRes = mockMvc.perform(get("/api/usuarios/get/{id}", conflictId));
        ResultActions dataConflictRes = mockMvc.perform(get("/api/usuarios/get/{id}", dataConflictId));
        ResultActions timeoutRes = mockMvc.perform(get("/api/usuarios/get/{id}", timeoutId));
        ResultActions rejectedRes = mockMvc.perform(get("/api/usuarios/get/{id}", rejectedId));
        ResultActions internalErrorRes = mockMvc.perform(get("/api/usuarios/get/{id}", errorId));

        // Assert
        typeMismatchRes.andExpect(status().isBadRequest());
        malformedJsonRes.andExpect(status().isBadRequest());
        methodNotAllowedRes.andExpect(status().isMethodNotAllowed());
        notFoundRes.andExpect(status().isNotFound());
        conflictRes.andExpect(status().isConflict());
        dataConflictRes.andExpect(status().isConflict());
        timeoutRes.andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.status").value(504))
                .andExpect(jsonPath("$.message").value("Tiempo de espera agotado al comunicarse con un servicio externo"));
        rejectedRes.andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("El servidor está saturado temporalmente, intente nuevamente"));
        internalErrorRes.andExpect(status().isInternalServerError());
    }

    @Test
    void shouldCreateBoundedNotificationAsyncExecutorBean() {
        // Arrange
        AsyncConfig asyncConfig = new AsyncConfig();

        // Act
        Executor executor = asyncConfig.notificationAsyncExecutor();

        // Assert
        assertNotNull(executor);
    }
}
