package com.finance.account.infraestructure.entry_points;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.account.domain.gateway.TokenGateway;
import com.finance.account.domain.model.Usuario;
import com.finance.account.domain.usecase.UsuarioUseCase;
import com.finance.account.infraestructure.config.JwtAuthenticationFilter;
import com.finance.account.infraestructure.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UsuarioController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioUseCase usuarioUseCase;

    @MockBean
    private TokenGateway tokenGateway;

    @Test
    @DisplayName("Should return 401 Unauthorized when requesting without authentication token")
    void shouldDenyAccessWhenUnauthenticated() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/api/usuarios/get/{id}", userId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Should return 200 OK when fetching user by ID with authentication")
    @WithMockUser
    void shouldGetUserByIdWhenAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();
        Usuario mockUser = new Usuario(
                userId,
                "test@example.com",
                "hashedpassword",
                "Test User",
                null,
                "USD",
                true
        );

        when(usuarioUseCase.obtenerUsuarioPorId(eq(userId))).thenReturn(mockUser);

        mockMvc.perform(get("/api/usuarios/get/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    @DisplayName("Should return 200 OK when fetching user by ID using standard REST alias /{id}")
    @WithMockUser
    void shouldGetUserByIdUsingRestAlias() throws Exception {
        UUID userId = UUID.randomUUID();
        Usuario mockUser = new Usuario(
                userId,
                "alias@example.com",
                "hashedpassword",
                "Alias User",
                null,
                "USD",
                true
        );

        when(usuarioUseCase.obtenerUsuarioPorId(eq(userId))).thenReturn(mockUser);

        mockMvc.perform(get("/api/usuarios/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("alias@example.com"));
    }

    @Test
    @DisplayName("Should return 200 OK when fetching user by Email with authentication")
    @WithMockUser
    void shouldGetUserByEmailWhenAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();
        String email = "juan@example.com";
        Usuario mockUser = new Usuario(
                userId,
                email,
                "hashedpassword",
                "Juan Perez",
                null,
                "USD",
                true
        );

        when(usuarioUseCase.obtenerUsuarioPorEmail(email)).thenReturn(mockUser);

        mockMvc.perform(get("/api/usuarios/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.fullName").value("Juan Perez"));
    }

    @Test
    @DisplayName("Should return 201 Created when saving user")
    @WithMockUser
    void shouldSaveUserWhenAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();
        Usuario inputUser = new Usuario();
        inputUser.setEmail("new@example.com");
        inputUser.setFullName("New User");

        Usuario savedUser = new Usuario(
                userId,
                "new@example.com",
                "hashedpass",
                "New User",
                null,
                "USD",
                true
        );

        when(usuarioUseCase.guardarUsuario(any(Usuario.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/usuarios/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    @DisplayName("Should return 200 OK when updating user")
    @WithMockUser
    void shouldUpdateUserWhenAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();
        Usuario updateUser = new Usuario();
        updateUser.setId(userId);
        updateUser.setFullName("Updated Name");

        when(usuarioUseCase.actualizarUsuario(any(Usuario.class))).thenReturn(updateUser);

        mockMvc.perform(put("/api/usuarios/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Name"));
    }

    @Test
    @DisplayName("Should return 200 OK when deleting user")
    @WithMockUser
    void shouldDeleteUserWhenAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(usuarioUseCase).eliminarUsuarioPorId(userId);

        mockMvc.perform(delete("/api/usuarios/delete/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuario eliminado exitosamente"));
    }

    @Test
    @DisplayName("Should authenticate using Bearer token via JwtAuthenticationFilter")
    void shouldAuthenticateWithBearerToken() throws Exception {
        UUID userId = UUID.randomUUID();
        String validToken = "valid.jwt.token";

        when(tokenGateway.validateToken(validToken)).thenReturn(true);
        when(tokenGateway.extractEmail(validToken)).thenReturn("bearer@example.com");

        Usuario mockUser = new Usuario(
                userId,
                "bearer@example.com",
                "hashedpassword",
                "Bearer User",
                null,
                "USD",
                true
        );
        when(usuarioUseCase.obtenerUsuarioPorId(eq(userId))).thenReturn(mockUser);

        mockMvc.perform(get("/api/usuarios/get/{id}", userId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("bearer@example.com"));
    }

    @Test
    @DisplayName("Should authenticate even if token is sent without Bearer prefix")
    void shouldAuthenticateWithRawToken() throws Exception {
        UUID userId = UUID.randomUUID();
        String validToken = "valid.raw.token";

        when(tokenGateway.validateToken(validToken)).thenReturn(true);
        when(tokenGateway.extractEmail(validToken)).thenReturn("raw@example.com");

        Usuario mockUser = new Usuario(
                userId,
                "raw@example.com",
                "hashedpassword",
                "Raw User",
                null,
                "USD",
                true
        );
        when(usuarioUseCase.obtenerUsuarioPorId(eq(userId))).thenReturn(mockUser);

        mockMvc.perform(get("/api/usuarios/get/{id}", userId)
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("raw@example.com"));
    }
}
