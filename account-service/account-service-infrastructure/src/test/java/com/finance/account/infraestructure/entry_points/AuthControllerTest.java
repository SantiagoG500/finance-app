package com.finance.account.infraestructure.entry_points;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.account.domain.model.AuthResult;
import com.finance.account.domain.model.Usuario;
import com.finance.account.domain.usecase.AuthUseCase;
import com.finance.account.infraestructure.entry_points.dto.AuthResponseDTO;
import com.finance.account.infraestructure.entry_points.dto.ChangePasswordRequestDTO;
import com.finance.account.infraestructure.entry_points.dto.LoginRequestDTO;
import com.finance.account.infraestructure.entry_points.dto.MessageResponseDTO;
import com.finance.account.infraestructure.entry_points.dto.RegisterRequestDTO;
import com.finance.account.infraestructure.entry_points.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private AuthUseCase authUseCase;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldRegisterUserAndReturn201CreatedWhenRequestIsValid() {
        // Arrange
        UUID userId = UUID.randomUUID();
        RegisterRequestDTO request = new RegisterRequestDTO("new@example.com", "password123", "New User");
        Usuario usuario = new Usuario(userId, "new@example.com", "hash", "New User", null, "USD", true);
        AuthResult authResult = new AuthResult("jwt-token", usuario);

        when(authUseCase.register("new@example.com", "password123", "New User")).thenReturn(authResult);

        // Act
        ResponseEntity<AuthResponseDTO> response = authController.register(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt-token", response.getBody().getToken());
        assertEquals("Bearer", response.getBody().getType());
        assertEquals(userId, response.getBody().getUserId());
        assertEquals("new@example.com", response.getBody().getEmail());
        assertEquals("New User", response.getBody().getFullName());
    }

    @Test
    void shouldReturn400BadRequestWhenRegisterValidationFails() throws Exception {
        // Arrange
        RegisterRequestDTO invalidRequest = new RegisterRequestDTO("invalid-email", "123", "Name");

        // Act
        ResultActions response = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldLoginAndReturn200OkWithAuthorizationHeaderAndMessageBodyWhenCredentialsAreValid() {
        // Arrange
        UUID userId = UUID.randomUUID();
        LoginRequestDTO request = new LoginRequestDTO("login@example.com", "password123");
        Usuario usuario = new Usuario(userId, "login@example.com", "hash", "Login User", null, "USD", true);
        AuthResult authResult = new AuthResult("jwt-login", usuario);

        when(authUseCase.login("login@example.com", "password123")).thenReturn(authResult);

        // Act
        ResponseEntity<MessageResponseDTO> response = authController.login(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Bearer jwt-login", response.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        assertNotNull(response.getBody());
        assertEquals("Se ha iniciado sesión correctamente", response.getBody().getMessage());
    }

    @Test
    void shouldReturn400BadRequestWhenLoginCredentialsAreInvalid() throws Exception {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO("login@example.com", "wrongPass");
        when(authUseCase.login("login@example.com", "wrongPass"))
                .thenThrow(new IllegalArgumentException("Credenciales inválidas"));

        // Act
        ResultActions response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }

    @Test
    void shouldLogoutAndReturn200Ok() throws Exception {
        // Arrange
        String header = "Bearer jwt-token-123";
        doNothing().when(authUseCase).logout(header);

        // Act
        ResultActions response = mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", header));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sesión cerrada exitosamente"));
        verify(authUseCase).logout(header);
    }

    @Test
    void shouldChangePasswordWithPatchAndReturn200OkWhenAuthenticatedOwner() {
        // Arrange
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO(
                "user@example.com",
                "oldPass123",
                "newPass1234"
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "user@example.com", null, Collections.emptyList()
        );
        doNothing().when(authUseCase).changePassword("user@example.com", "oldPass123", "newPass1234");

        // Act
        ResponseEntity<MessageResponseDTO> response = authController.changePassword(request, auth);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Contraseña actualizada correctamente", response.getBody().getMessage());
        verify(authUseCase).changePassword("user@example.com", "oldPass123", "newPass1234");
    }

    @Test
    void shouldReturn403ForbiddenWhenChangePasswordEmailDoesNotMatchAuthenticatedUser() throws Exception {
        // Arrange
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO(
                "victim@example.com",
                "oldPass123",
                "newPass1234"
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "attacker@example.com", null, Collections.emptyList()
        );

        // Act
        ResultActions response = mockMvc.perform(patch("/api/auth/password")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        response.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("No tiene permisos para modificar esta cuenta"));
    }

    @Test
    void shouldReturn409ConflictWhenChangePasswordAccountIsInactive() throws Exception {
        // Arrange
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO(
                "inactive@example.com",
                "oldPass123",
                "newPass1234"
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "inactive@example.com", null, Collections.emptyList()
        );
        doThrow(new IllegalStateException("La cuenta de usuario está desactivada"))
                .when(authUseCase)
                .changePassword("inactive@example.com", "oldPass123", "newPass1234");

        // Act
        ResultActions response = mockMvc.perform(patch("/api/auth/password")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        response.andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("La cuenta de usuario está desactivada"));
    }
}
