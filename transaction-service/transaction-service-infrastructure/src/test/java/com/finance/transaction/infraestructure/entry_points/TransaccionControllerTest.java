package com.finance.transaction.infraestructure.entry_points;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.finance.transaction.domain.model.Transaccion;
import com.finance.transaction.domain.model.enumns.FrecuenciaType;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import com.finance.transaction.domain.usecase.TransaccionUseCase;
import com.finance.transaction.infraestructure.entry_points.dto.MessageResponseDTO;
import com.finance.transaction.infraestructure.entry_points.dto.TransaccionRequestDTO;
import com.finance.transaction.infraestructure.entry_points.dto.TransaccionResponseDTO;
import com.finance.transaction.infraestructure.entry_points.exception.GlobalExceptionHandler;
import com.finance.transaction.infraestructure.mapper.TransaccionMapper;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransaccionControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private String userId;

    private Jwt validJwt;

    @Mock
    private TransaccionUseCase transaccionUseCase;

    @Mock
    private TransaccionMapper transaccionMapper;

    @InjectMocks
    private TransaccionController transaccionController;

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
        mockMvc = MockMvcBuilders.standaloneSetup(transaccionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSaveTransactionAndReturn201CreatedWhenRequestIsValid() {
        // Arrange
        String txId = UUID.randomUUID().toString();
        String catId = UUID.randomUUID().toString();
        TransaccionRequestDTO request = new TransaccionRequestDTO(
                txId,
                catId,
                new BigDecimal("500.00"),
                "USD",
                TransaccionType.INCOME,
                FrecuenciaType.MONTHLY,
                "Salario",
                LocalDate.now()
        );
        Transaccion domain = buildDomain(txId, catId);
        TransaccionResponseDTO responseDto = buildResponseDto(txId, catId);

        when(transaccionMapper.requestDtoToTransaccion(request, userId)).thenReturn(domain);
        when(transaccionUseCase.guardarTransaccion(domain, "user@finance.com")).thenReturn(domain);
        when(transaccionMapper.transaccionToResponseDto(domain)).thenReturn(responseDto);

        // Act
        ResponseEntity<TransaccionResponseDTO> response = transaccionController.guardarTransaccion(request, validJwt);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(txId, response.getBody().getId());
    }

    @Test
    void shouldReturn400BadRequestWhenSaveTransactionPayloadIsInvalid() throws Exception {
        // Arrange
        String invalidJson = "{\"id\":\"\",\"categoryId\":\"\",\"amount\":-10,\"currency\":\"\",\"type\":null,\"frequency\":null,\"transactionDate\":null}";

        // Act
        // Assert
        mockMvc.perform(post("/api/v1/finances/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldGetTransactionByIdAndReturn200OkWhenFound() throws Exception {
        // Arrange
        String txId = UUID.randomUUID().toString();
        String catId = UUID.randomUUID().toString();
        Transaccion domain = buildDomain(txId, catId);
        TransaccionResponseDTO responseDto = buildResponseDto(txId, catId);

        when(transaccionUseCase.obtenerTransaccion(txId, userId)).thenReturn(domain);
        when(transaccionMapper.transaccionToResponseDto(domain)).thenReturn(responseDto);

        // Act
        // Assert
        mockMvc.perform(get("/api/v1/finances/get/{id}", txId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(txId))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    void shouldReturn404NotFoundWhenTransactionByIdIsMissing() throws Exception {
        // Arrange
        String txId = UUID.randomUUID().toString();
        when(transaccionUseCase.obtenerTransaccion(txId, userId)).thenReturn(null);

        // Act
        // Assert
        mockMvc.perform(get("/api/v1/finances/get/{id}", txId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Transacción no encontrada"));
    }

    @Test
    void shouldUpdateTransactionAndReturn200OkWhenRequestIsValid() {
        // Arrange
        String txId = UUID.randomUUID().toString();
        String catId = UUID.randomUUID().toString();
        TransaccionRequestDTO request = new TransaccionRequestDTO(
                txId,
                catId,
                new BigDecimal("750.00"),
                "USD",
                TransaccionType.EXPENSE,
                FrecuenciaType.MONTHLY,
                "Renta",
                LocalDate.now()
        );
        Transaccion domain = buildDomain(txId, catId);
        TransaccionResponseDTO responseDto = buildResponseDto(txId, catId);

        when(transaccionMapper.requestDtoToTransaccion(request, userId)).thenReturn(domain);
        when(transaccionUseCase.actualizarTransaccion(domain)).thenReturn(domain);
        when(transaccionMapper.transaccionToResponseDto(domain)).thenReturn(responseDto);

        // Act
        ResponseEntity<TransaccionResponseDTO> response = transaccionController.actualizarTransaccion(request, validJwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(txId, response.getBody().getId());
    }

    @Test
    void shouldDeleteTransactionAndReturn200OkWithMessageResponse() throws Exception {
        // Arrange
        String txId = UUID.randomUUID().toString();
        doNothing().when(transaccionUseCase).eliminarTransaccion(txId, userId);

        // Act
        // Assert
        mockMvc.perform(delete("/api/v1/finances/delete/{id}", txId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transacción eliminada exitosamente"));
    }

    @Test
    void shouldGetTransactionsByTypeAndReturn200OkWhenValid() throws Exception {
        // Arrange
        String txId = UUID.randomUUID().toString();
        String catId = UUID.randomUUID().toString();
        Transaccion domain = buildDomain(txId, catId);
        TransaccionResponseDTO responseDto = buildResponseDto(txId, catId);

        when(transaccionUseCase.obtenerTransaccionesPorTipo(userId, TransaccionType.INCOME))
                .thenReturn(List.of(domain));
        when(transaccionMapper.transaccionToResponseDto(domain)).thenReturn(responseDto);

        // Act
        // Assert
        mockMvc.perform(get("/api/v1/finances/transactions/by-type")
                        .param("type", "INCOME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(txId));
    }

    @Test
    void shouldReturn400BadRequestWhenTypeParamIsInvalidEnumOrMissing() throws Exception {
        // Arrange
        String invalidType = "INVALID_TYPE";

        // Act
        // Assert
        mockMvc.perform(get("/api/v1/finances/transactions/by-type")
                        .param("type", invalidType))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        mockMvc.perform(get("/api/v1/finances/transactions/by-type"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400BadRequestWhenJwtIsMissingOrSubjectIsBlank() throws Exception {
        // Arrange
        SecurityContextHolder.clearContext();
        String txId = UUID.randomUUID().toString();

        // Act
        // Assert
        mockMvc.perform(get("/api/v1/finances/get/{id}", txId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("El token JWT no contiene un identificador de usuario válido"));
    }

    private Transaccion buildDomain(String txId, String catId) {
        return new Transaccion(
                txId,
                userId,
                catId,
                new BigDecimal("500.00"),
                "USD",
                TransaccionType.INCOME,
                FrecuenciaType.MONTHLY,
                "Salario",
                LocalDate.now(),
                LocalDateTime.now()
        );
    }

    private TransaccionResponseDTO buildResponseDto(String txId, String catId) {
        return TransaccionResponseDTO.builder()
                .id(txId)
                .categoryId(catId)
                .amount(new BigDecimal("500.00"))
                .currency("USD")
                .type(TransaccionType.INCOME)
                .frequency(FrecuenciaType.MONTHLY)
                .description("Salario")
                .transactionDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
