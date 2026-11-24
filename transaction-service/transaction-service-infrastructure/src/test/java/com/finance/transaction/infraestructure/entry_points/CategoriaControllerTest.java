package com.finance.transaction.infraestructure.entry_points;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.transaction.domain.model.Categoria;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import com.finance.transaction.domain.usecase.CategoriaUseCase;
import com.finance.transaction.infraestructure.entry_points.dto.CategoriaResponseDTO;
import com.finance.transaction.infraestructure.entry_points.dto.CreateCategoriaRequestDTO;
import com.finance.transaction.infraestructure.entry_points.exception.GlobalExceptionHandler;
import com.finance.transaction.infraestructure.mapper.CategoriaMapper;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CategoriaControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private String userId;

    private Jwt validJwt;

    @Mock
    private CategoriaUseCase categoriaUseCase;

    @Mock
    private CategoriaMapper categoriaMapper;

    @InjectMocks
    private CategoriaController categoriaController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
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
        mockMvc = MockMvcBuilders.standaloneSetup(categoriaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateCategoryAndReturn201CreatedWhenRequestIsValid() {
        // Arrange
        String catId = UUID.randomUUID().toString();
        CreateCategoriaRequestDTO request = new CreateCategoriaRequestDTO("Salario", TransaccionType.INCOME);
        Categoria domain = new Categoria(catId, userId, "Salario", TransaccionType.INCOME);
        CategoriaResponseDTO responseDto = CategoriaResponseDTO.builder()
                .id(catId)
                .userId(userId)
                .name("Salario")
                .type(TransaccionType.INCOME)
                .build();

        when(categoriaMapper.requestDtoToCategoria(request, userId)).thenReturn(domain);
        when(categoriaUseCase.crearCategoria(domain, "Bearer mock-jwt-token")).thenReturn(domain);
        when(categoriaMapper.categoriaToResponseDto(domain)).thenReturn(responseDto);

        // Act
        ResponseEntity<CategoriaResponseDTO> response = categoriaController.crearCategoria(
                request,
                validJwt,
                "Bearer mock-jwt-token"
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(catId, response.getBody().getId());
        assertEquals(userId, response.getBody().getUserId());
        assertEquals("Salario", response.getBody().getName());
        assertEquals(TransaccionType.INCOME, response.getBody().getType());
    }

    @Test
    void shouldReturn400BadRequestWhenCreateCategoryPayloadIsInvalid() throws Exception {
        // Arrange
        String invalidJson = "{\"name\":\"\",\"type\":null}";

        // Act
        ResultActions response = mockMvc.perform(post("/api/v1/finances/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldListCategoriesByUserAndReturn200Ok() throws Exception {
        // Arrange
        String catId = UUID.randomUUID().toString();
        Categoria domain = new Categoria(catId, userId, "Salario", TransaccionType.INCOME);
        CategoriaResponseDTO responseDto = new CategoriaResponseDTO(catId, userId, "Salario", TransaccionType.INCOME);

        when(categoriaUseCase.listarCategoriasPorUsuario(eq(userId), any())).thenReturn(List.of(domain));
        when(categoriaMapper.categoriaToResponseDto(domain)).thenReturn(responseDto);

        // Act
        ResultActions response = mockMvc.perform(get("/api/v1/finances/categories")
                .header("Authorization", "Bearer mock-jwt-token"));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(catId))
                .andExpect(jsonPath("$[0].userId").value(userId))
                .andExpect(jsonPath("$[0].name").value("Salario"))
                .andExpect(jsonPath("$[0].type").value("INCOME"));
    }

    @Test
    void shouldDeleteCategoryAndReturn204NoContentWhenValid() throws Exception {
        // Arrange
        String catId = UUID.randomUUID().toString();
        doNothing().when(categoriaUseCase).eliminarCategoria(eq(catId), eq(userId), any());

        // Act
        ResultActions response = mockMvc.perform(delete("/api/v1/finances/categories/{id}", catId));

        // Assert
        response.andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404NotFoundOr409ConflictWhenDeleteCategoryFails() throws Exception {
        // Arrange
        String missingId = UUID.randomUUID().toString();
        String inUseId = UUID.randomUUID().toString();

        doThrow(new NoSuchElementException("Categoría no encontrada"))
                .when(categoriaUseCase).eliminarCategoria(eq(missingId), eq(userId), any());
        doThrow(new IllegalStateException("No se puede eliminar la categoría porque tiene transacciones asociadas"))
                .when(categoriaUseCase).eliminarCategoria(eq(inUseId), eq(userId), any());

        // Act
        ResultActions notFoundRes = mockMvc.perform(delete("/api/v1/finances/categories/{id}", missingId));
        ResultActions conflictRes = mockMvc.perform(delete("/api/v1/finances/categories/{id}", inUseId));

        // Assert
        notFoundRes.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Categoría no encontrada"));
        conflictRes.andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("No se puede eliminar la categoría porque tiene transacciones asociadas"));
    }
}
