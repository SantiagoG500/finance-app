package com.finance.transaction.infraestructure.entry_points;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.finance.transaction.domain.model.Transaccion;
import com.finance.transaction.domain.model.enumns.FrecuenciaType;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import com.finance.transaction.domain.usecase.TransaccionUseCase;
import com.finance.transaction.infraestructure.entry_points.dto.CreateTransaccionRequestDTO;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                .subject("user@finance.com")
                .claim("userId", userId)
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
    void shouldSaveTransactionAndReturn201CreatedWithUserIdWhenRequestIsValid() {
        // Arrange
        String txId = UUID.randomUUID().toString();
        String catId = UUID.randomUUID().toString();
        CreateTransaccionRequestDTO request = new CreateTransaccionRequestDTO(
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
        when(transaccionUseCase.guardarTransaccion(domain, "user@finance.com", "Bearer mock-jwt-token")).thenReturn(domain);
        when(transaccionMapper.transaccionToResponseDto(domain)).thenReturn(responseDto);

        // Act
        ResponseEntity<TransaccionResponseDTO> response = transaccionController.guardarTransaccion(
                request,
                validJwt,
                "Bearer mock-jwt-token"
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(txId, response.getBody().getId());
        assertEquals(userId, response.getBody().getUserId());
    }

    @Test
    void shouldReturn400BadRequestWhenSaveTransactionPayloadIsInvalid() throws Exception {
        // Arrange
        String invalidJson = "{\"categoryId\":\"\",\"amount\":-10,\"currency\":\"INVALID\",\"type\":null,\"frequency\":null,\"transactionDate\":null}";

        // Act
        ResultActions response = mockMvc.perform(post("/api/v1/finances/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400BadRequestWhenDescriptionIsBlankOnSaveTransaction() throws Exception {
        // Arrange
        String catId = UUID.randomUUID().toString();
        CreateTransaccionRequestDTO missingDescriptionRequest = new CreateTransaccionRequestDTO(
                catId,
                new BigDecimal("150.00"),
                "USD",
                TransaccionType.EXPENSE,
                FrecuenciaType.ONE_TIME,
                "   ",
                LocalDate.now()
        );

        // Act
        ResultActions response = mockMvc.perform(post("/api/v1/finances/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missingDescriptionRequest)));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("La descripción de la transacción es obligatoria")));
    }

    @Test
    void shouldGetTransactionByIdAndReturn200OkWithUserIdWhenFound() throws Exception {
        // Arrange
        String txId = UUID.randomUUID().toString();
        String catId = UUID.randomUUID().toString();
        Transaccion domain = buildDomain(txId, catId);
        TransaccionResponseDTO responseDto = buildResponseDto(txId, catId);

        when(transaccionUseCase.obtenerTransaccion(eq(txId), eq(userId), any())).thenReturn(domain);
        when(transaccionMapper.transaccionToResponseDto(domain)).thenReturn(responseDto);

        // Act
        ResultActions response = mockMvc.perform(get("/api/v1/finances/get/{id}", txId)
                .header("Authorization", "Bearer mock-jwt-token"));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(txId))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    void shouldReturn404NotFoundOr401Or403Or409WhenGetTransactionByIdFails() throws Exception {
        // Arrange
        String missingId = UUID.randomUUID().toString();
        String unauthorizedId = UUID.randomUUID().toString();
        String forbiddenId = UUID.randomUUID().toString();
        String deactivatedId = UUID.randomUUID().toString();
        String conflictId = UUID.randomUUID().toString();

        when(transaccionUseCase.obtenerTransaccion(eq(missingId), eq(userId), any())).thenReturn(null);
        when(transaccionUseCase.obtenerTransaccion(eq(unauthorizedId), eq(userId), any()))
                .thenThrow(new SecurityException("Sesión expirada"));
        when(transaccionUseCase.obtenerTransaccion(eq(forbiddenId), eq(userId), any()))
                .thenThrow(new AccessDeniedException("Acceso denegado"));
        when(transaccionUseCase.obtenerTransaccion(eq(deactivatedId), eq(userId), any()))
                .thenThrow(new IllegalStateException("Cuenta desactivada. Reactiva tu cuenta para operar en transacciones y metas"));
        when(transaccionUseCase.obtenerTransaccion(eq(conflictId), eq(userId), any()))
                .thenThrow(new DataIntegrityViolationException("Constraint"));

        // Act
        ResultActions notFoundRes = mockMvc.perform(get("/api/v1/finances/get/{id}", missingId));
        ResultActions unauthorizedRes = mockMvc.perform(get("/api/v1/finances/get/{id}", unauthorizedId));
        ResultActions forbiddenRes = mockMvc.perform(get("/api/v1/finances/get/{id}", forbiddenId));
        ResultActions deactivatedRes = mockMvc.perform(get("/api/v1/finances/get/{id}", deactivatedId));
        ResultActions conflictRes = mockMvc.perform(get("/api/v1/finances/get/{id}", conflictId));

        // Assert
        notFoundRes.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Transacción no encontrada"));
        unauthorizedRes.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        forbiddenRes.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        deactivatedRes.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Cuenta desactivada. Reactiva tu cuenta para operar en transacciones y metas"));
        conflictRes.andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldGetTransactionsByTypeAndReturn200OkWhenValid() throws Exception {
        // Arrange
        String txId = UUID.randomUUID().toString();
        String catId = UUID.randomUUID().toString();
        Transaccion domain = buildDomain(txId, catId);
        TransaccionResponseDTO responseDto = buildResponseDto(txId, catId);

        when(transaccionUseCase.obtenerTransaccionesPorTipo(eq(userId), eq(TransaccionType.INCOME), any()))
                .thenReturn(List.of(domain));
        when(transaccionMapper.transaccionToResponseDto(domain)).thenReturn(responseDto);

        // Act
        ResultActions response = mockMvc.perform(get("/api/v1/finances/transactions/by-type")
                .param("type", "INCOME"));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(txId))
                .andExpect(jsonPath("$[0].userId").value(userId));
    }

    @Test
    void shouldReturn400BadRequestWhenTypeParamIsInvalidEnumOrMissing() throws Exception {
        // Arrange
        String invalidType = "INVALID_TYPE";

        // Act
        ResultActions badEnumRes = mockMvc.perform(get("/api/v1/finances/transactions/by-type")
                .param("type", invalidType));
        ResultActions missingParamRes = mockMvc.perform(get("/api/v1/finances/transactions/by-type"));

        // Assert
        badEnumRes.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        missingParamRes.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400BadRequestWhenJwtIsMissingOrSubjectIsBlank() throws Exception {
        // Arrange
        SecurityContextHolder.clearContext();
        String txId = UUID.randomUUID().toString();

        // Act
        ResultActions response = mockMvc.perform(get("/api/v1/finances/get/{id}", txId));

        // Assert
        response.andExpect(status().isBadRequest())
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
                .userId(userId)
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
