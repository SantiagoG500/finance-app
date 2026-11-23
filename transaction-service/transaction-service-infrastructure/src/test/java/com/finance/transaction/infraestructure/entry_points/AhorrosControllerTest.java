package com.finance.transaction.infraestructure.entry_points;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.finance.transaction.domain.model.MetaAhorro;
import com.finance.transaction.domain.usecase.MetaAhorrosUseCase;
import com.finance.transaction.infraestructure.entry_points.dto.AbonoMetaRequestDTO;
import com.finance.transaction.infraestructure.entry_points.dto.MetaAhorroRequestDTO;
import com.finance.transaction.infraestructure.entry_points.dto.MetaAhorroResponseDTO;
import com.finance.transaction.infraestructure.entry_points.exception.GlobalExceptionHandler;
import com.finance.transaction.infraestructure.mapper.MetaAhorrosMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AhorrosControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private String userId;

    private Jwt validJwt;

    @Mock
    private MetaAhorrosUseCase metaAhorrosUseCase;

    @Mock
    private MetaAhorrosMapper metaAhorrosMapper;

    @InjectMocks
    private AhorrosController ahorrosController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        userId = UUID.randomUUID().toString();
        validJwt = Jwt.withTokenValue("mock-jwt-token")
                .header("alg", "HS256")
                .subject(userId)
                .claim("email", "user@finance.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(validJwt));
        mockMvc = MockMvcBuilders.standaloneSetup(ahorrosController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSaveGoalAndReturn201CreatedWhenRequestIsValid() {
        // Arrange
        String goalId = UUID.randomUUID().toString();
        MetaAhorroRequestDTO request = new MetaAhorroRequestDTO(
                "Viaje",
                new BigDecimal("2000.00"),
                new BigDecimal("200.00"),
                new BigDecimal("100.00"),
                "USD",
                LocalDate.now().plusMonths(12)
        );
        MetaAhorro domain = buildDomain(goalId);
        MetaAhorroResponseDTO responseDto = buildResponseDto(goalId);

        when(metaAhorrosMapper.requestDtoToMetaAhorro(request, userId)).thenReturn(domain);
        when(metaAhorrosUseCase.guardarMetaAhorros(domain)).thenReturn(domain);
        when(metaAhorrosMapper.metaAhorroToResponseDto(domain)).thenReturn(responseDto);

        // Act
        ResponseEntity<MetaAhorroResponseDTO> response = ahorrosController.guardarMetaAhorro(request, validJwt);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(goalId, response.getBody().getId());
    }

    @Test
    void shouldReturn400BadRequestWhenSaveGoalValidationFails() throws Exception {
        // Arrange
        String invalidJson = "{\"title\":\"\",\"targetAmount\":-10,\"currentAmount\":-1,\"monthlyContribution\":0,\"currency\":\"\",\"targetDate\":\"2020-01-01\"}";

        // Act
        // Assert
        mockMvc.perform(post("/api/v1/finances/goals/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldGetGoalByIdAndReturn200OkWhenFound() throws Exception {
        // Arrange
        String goalId = UUID.randomUUID().toString();
        MetaAhorro domain = buildDomain(goalId);
        MetaAhorroResponseDTO responseDto = buildResponseDto(goalId);

        when(metaAhorrosUseCase.obtenerMetaAhorros(goalId, userId)).thenReturn(domain);
        when(metaAhorrosMapper.metaAhorroToResponseDto(domain)).thenReturn(responseDto);

        // Act
        // Assert
        mockMvc.perform(get("/api/v1/finances/goals/get/{id}", goalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(goalId))
                .andExpect(jsonPath("$.title").value("Viaje"));
    }

    @Test
    void shouldReturn404NotFoundWhenGoalByIdDoesNotExist() throws Exception {
        // Arrange
        String goalId = UUID.randomUUID().toString();
        when(metaAhorrosUseCase.obtenerMetaAhorros(goalId, userId)).thenReturn(null);

        // Act
        // Assert
        mockMvc.perform(get("/api/v1/finances/goals/get/{id}", goalId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Meta de ahorro no encontrada"));
    }

    @Test
    void shouldGetGoalsByUserAndReturn200Ok() throws Exception {
        // Arrange
        String goalId = UUID.randomUUID().toString();
        MetaAhorro domain = buildDomain(goalId);
        MetaAhorroResponseDTO responseDto = buildResponseDto(goalId);

        when(metaAhorrosUseCase.obtenerMetasPorUsuario(userId)).thenReturn(List.of(domain));
        when(metaAhorrosMapper.metaAhorroToResponseDto(domain)).thenReturn(responseDto);

        // Act
        // Assert
        mockMvc.perform(get("/api/v1/finances/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(goalId));
    }

    @Test
    void shouldUpdateGoalAndReturn200OkWhenRequestIsValid() {
        // Arrange
        String goalId = UUID.randomUUID().toString();
        MetaAhorroRequestDTO request = new MetaAhorroRequestDTO(
                "Viaje Europa",
                new BigDecimal("3000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("250.00"),
                "USD",
                LocalDate.now().plusMonths(12)
        );
        MetaAhorro domain = buildDomain(goalId);
        MetaAhorroResponseDTO responseDto = buildResponseDto(goalId);

        when(metaAhorrosMapper.requestDtoToMetaAhorro(goalId, request, userId)).thenReturn(domain);
        when(metaAhorrosUseCase.actualizarMetaAhorros(domain)).thenReturn(domain);
        when(metaAhorrosMapper.metaAhorroToResponseDto(domain)).thenReturn(responseDto);

        // Act
        ResponseEntity<MetaAhorroResponseDTO> response = ahorrosController.actualizarMetaAhorro(goalId, request, validJwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(goalId, response.getBody().getId());
    }

    @Test
    void shouldDeleteGoalAndReturn200OkWithMessageResponse() throws Exception {
        // Arrange
        String goalId = UUID.randomUUID().toString();
        doNothing().when(metaAhorrosUseCase).eliminarMetaAhorros(goalId, userId);

        // Act
        // Assert
        mockMvc.perform(delete("/api/v1/finances/goals/delete/{id}", goalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Meta de ahorro eliminada exitosamente"));
    }

    @Test
    void shouldDepositToGoalAndReturn200OkWhenRequestIsValid() throws Exception {
        // Arrange
        String goalId = UUID.randomUUID().toString();
        AbonoMetaRequestDTO request = new AbonoMetaRequestDTO(new BigDecimal("250.00"));
        MetaAhorro updatedGoal = buildDomain(goalId);
        MetaAhorroResponseDTO responseDto = buildResponseDto(goalId);

        when(metaAhorrosUseCase.abonarMetaAhorros(goalId, userId, new BigDecimal("250.00"), "user@finance.com"))
                .thenReturn(updatedGoal);
        when(metaAhorrosMapper.metaAhorroToResponseDto(updatedGoal)).thenReturn(responseDto);

        // Act
        // Assert
        mockMvc.perform(put("/api/v1/finances/goals/{id}/deposit", goalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(goalId));
    }

    @Test
    void shouldReturn400BadRequestWhenDepositValidationFails() throws Exception {
        // Arrange
        String goalId = UUID.randomUUID().toString();
        AbonoMetaRequestDTO invalidRequest = new AbonoMetaRequestDTO(BigDecimal.ZERO);

        // Act
        // Assert
        mockMvc.perform(put("/api/v1/finances/goals/{id}/deposit", goalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn404NotFoundWhenDepositingToNonExistentGoal() throws Exception {
        // Arrange
        String goalId = UUID.randomUUID().toString();
        AbonoMetaRequestDTO request = new AbonoMetaRequestDTO(new BigDecimal("100.00"));
        when(metaAhorrosUseCase.abonarMetaAhorros(goalId, userId, new BigDecimal("100.00"), "user@finance.com"))
                .thenThrow(new NoSuchElementException("Meta de ahorro no encontrada"));

        // Act
        // Assert
        mockMvc.perform(put("/api/v1/finances/goals/{id}/deposit", goalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn400BadRequestWhenConstraintViolationExceptionOccurs() throws Exception {
        // Arrange
        String goalId = UUID.randomUUID().toString();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("ID no debe estar vacío");
        ConstraintViolationException exception = new ConstraintViolationException("Error de validación", Set.of(violation));
        when(metaAhorrosUseCase.obtenerMetaAhorros(goalId, userId)).thenThrow(exception);

        // Act
        // Assert
        mockMvc.perform(get("/api/v1/finances/goals/get/{id}", goalId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("ID no debe estar vacío"));
    }

    @Test
    void shouldReturn400BadRequestWhenJwtIsMissing() throws Exception {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act
        // Assert
        mockMvc.perform(get("/api/v1/finances/goals"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    private MetaAhorro buildDomain(String goalId) {
        return new MetaAhorro(
                goalId,
                userId,
                "Viaje",
                new BigDecimal("2000.00"),
                new BigDecimal("450.00"),
                new BigDecimal("100.00"),
                "USD",
                LocalDate.now().plusMonths(12),
                LocalDateTime.now()
        );
    }

    private MetaAhorroResponseDTO buildResponseDto(String goalId) {
        return MetaAhorroResponseDTO.builder()
                .id(goalId)
                .title("Viaje")
                .targetAmount(new BigDecimal("2000.00"))
                .currentAmount(new BigDecimal("450.00"))
                .monthlyContribution(new BigDecimal("100.00"))
                .currency("USD")
                .targetDate(LocalDate.now().plusMonths(12))
                .createdAt(LocalDateTime.now())
                .build();
    }
}
