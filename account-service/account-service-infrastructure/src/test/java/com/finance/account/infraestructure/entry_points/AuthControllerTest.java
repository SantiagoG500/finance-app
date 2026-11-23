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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        // Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldLoginAndReturn200OkWhenCredentialsAreValid() {
        // Arrange
        UUID userId = UUID.randomUUID();
        LoginRequestDTO request = new LoginRequestDTO("login@example.com", "password123");
        Usuario usuario = new Usuario(userId, "login@example.com", "hash", "Login User", null, "USD", true);
        AuthResult authResult = new AuthResult("jwt-login", usuario);

        when(authUseCase.login("login@example.com", "password123")).thenReturn(authResult);

        // Act
        ResponseEntity<AuthResponseDTO> response = authController.login(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt-login", response.getBody().getToken());
        assertEquals("login@example.com", response.getBody().getEmail());
    }

    @Test
    void shouldReturn400BadRequestWhenLoginCredentialsAreInvalid() throws Exception {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO("login@example.com", "wrongPass");
        when(authUseCase.login("login@example.com", "wrongPass"))
                .thenThrow(new IllegalArgumentException("Credenciales inválidas"));

        // Act
        // Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }

    @Test
    void shouldReturn409ConflictWhenLoginAccountIsInactive() throws Exception {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO("inactive@example.com", "password123");
        when(authUseCase.login("inactive@example.com", "password123"))
                .thenThrow(new IllegalStateException("La cuenta de usuario está desactivada"));

        // Act
        // Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("La cuenta de usuario está desactivada"));
    }

    @Test
    void shouldChangePasswordAndReturn200OkWithMessageResponseDTO() {
        // Arrange
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO(
                "user@example.com",
                "oldPass123",
                "newPass1234"
        );
        doNothing().when(authUseCase).changePassword("user@example.com", "oldPass123", "newPass1234");

        // Act
        ResponseEntity<MessageResponseDTO> response = authController.changePassword(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Contraseña actualizada correctamente", response.getBody().getMessage());
        verify(authUseCase).changePassword("user@example.com", "oldPass123", "newPass1234");
    }

    @Test
    void shouldReturn400BadRequestWhenChangePasswordValidationFails() throws Exception {
        // Arrange
        ChangePasswordRequestDTO invalidRequest = new ChangePasswordRequestDTO(
                "user@example.com",
                "oldPass",
                "short"
        );

        // Act
        // Assert
        mockMvc.perform(put("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn409ConflictWhenChangePasswordAccountIsInactive() throws Exception {
        // Arrange
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO(
                "inactive@example.com",
                "oldPass123",
                "newPass1234"
        );
        doThrow(new IllegalStateException("La cuenta de usuario está desactivada"))
                .when(authUseCase)
                .changePassword("inactive@example.com", "oldPass123", "newPass1234");

        // Act
        // Assert
        mockMvc.perform(put("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("La cuenta de usuario está desactivada"));
    }
}
