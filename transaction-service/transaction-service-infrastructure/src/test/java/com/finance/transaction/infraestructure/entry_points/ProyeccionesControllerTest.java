package com.finance.transaction.infraestructure.entry_points;

import com.finance.transaction.domain.model.ConvertedBalance;
import com.finance.transaction.domain.model.CurrencyMetadata;
import com.finance.transaction.domain.model.ResumenFinanciero;
import com.finance.transaction.domain.usecase.DivisasUseCase;
import com.finance.transaction.domain.usecase.ProyeccionesUseCase;
import com.finance.transaction.infraestructure.entry_points.dto.ResumenFinancieroDTO;
import com.finance.transaction.infraestructure.entry_points.exception.GlobalExceptionHandler;
import com.finance.transaction.infraestructure.mapper.ResumenFinancieroMapper;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProyeccionesControllerTest {

    private MockMvc mockMvc;

    private String userId;

    @Mock
    private ProyeccionesUseCase proyeccionesUseCase;

    @Mock
    private ResumenFinancieroMapper resumenFinancieroMapper;

    @Mock
    private DivisasUseCase divisasUseCase;

    @InjectMocks
    private ProyeccionesController proyeccionesController;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
        Jwt validJwt = Jwt.withTokenValue("mock-jwt-token")
                .header("alg", "HS256")
                .subject(userId)
                .claim("email", "user@finance.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(validJwt));
        mockMvc = MockMvcBuilders.standaloneSetup(proyeccionesController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldGetFinancialSummaryAndReturn200Ok() throws Exception {
        // Arrange
        ResumenFinanciero resumen = new ResumenFinanciero();
        resumen.setUsuarioId(userId);
        resumen.setMoneda("USD");
        ResumenFinancieroDTO dto = ResumenFinancieroDTO.builder()
                .usuarioId(userId)
                .moneda("USD")
                .ingresosMensuales(new BigDecimal("3000.00"))
                .gastosMensuales(new BigDecimal("1000.00"))
                .balanceNetoMensual(new BigDecimal("2000.00"))
                .compromisoAhorroMensual(new BigDecimal("500.00"))
                .margenLibre(new BigDecimal("1500.00"))
                .metasAhorro(Collections.emptyList())
                .proyeccion1Ano(new BigDecimal("24000.00"))
                .build();

        when(proyeccionesUseCase.obtenerResumenFinanciero(userId)).thenReturn(resumen);
        when(resumenFinancieroMapper.resumenFinancieroToDto(resumen)).thenReturn(dto);

        // Act
        // Assert
        mockMvc.perform(get("/api/usuarios/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(userId))
                .andExpect(jsonPath("$.balanceNetoMensual").value(2000.00));
    }

    @Test
    void shouldProjectBalanceAndReturn200OkWhenMonthsIsInRange() throws Exception {
        // Arrange
        when(proyeccionesUseCase.proyectarSaldo(userId, 12, "user@finance.com"))
                .thenReturn(new BigDecimal("12000.00"));

        // Act
        // Assert
        mockMvc.perform(get("/api/usuarios/projections").param("months", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(userId))
                .andExpect(jsonPath("$.mesesProyectados").value(12))
                .andExpect(jsonPath("$.saldoProyectado").value(12000.00));
    }

    @Test
    void shouldReturn400BadRequestWhenMonthsIsOutOfRange() throws Exception {
        // Arrange
        String belowRange = "0";
        String aboveRange = "121";

        // Act
        // Assert
        mockMvc.perform(get("/api/usuarios/projections").param("months", belowRange))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("El horizonte de meses debe estar entre 1 y 120"));
        mockMvc.perform(get("/api/usuarios/projections").param("months", aboveRange))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldGetNetBalanceAndReturn200Ok() throws Exception {
        // Arrange
        when(proyeccionesUseCase.calcularBalanceNeto(userId)).thenReturn(new BigDecimal("1850.50"));

        // Act
        // Assert
        mockMvc.perform(get("/api/usuarios/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(userId))
                .andExpect(jsonPath("$.balanceNetoMensual").value(1850.50));
    }

    @Test
    void shouldConvertMonthlyBalanceAndReturn200Ok() throws Exception {
        // Arrange
        ConvertedBalance converted = new ConvertedBalance(
                userId,
                "USD",
                "EUR",
                new BigDecimal("0.92"),
                new BigDecimal("1000.00"),
                new BigDecimal("920.00")
        );
        when(divisasUseCase.convertirBalanceMensual(userId, "USD", "EUR")).thenReturn(converted);

        // Act
        // Assert
        mockMvc.perform(get("/api/usuarios/balance/convert")
                        .param("from", "USD")
                        .param("to", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(userId))
                .andExpect(jsonPath("$.baseCurrency").value("USD"))
                .andExpect(jsonPath("$.targetCurrency").value("EUR"))
                .andExpect(jsonPath("$.convertedBalance").value(920.00));
    }

    @Test
    void shouldGetCurrencyMetadataAndReturn200Ok() throws Exception {
        // Arrange
        CurrencyMetadata metadata = new CurrencyMetadata("COP", "Colombian peso", "$", List.of("Colombia"));
        when(divisasUseCase.consultarMetadatosMoneda("COP")).thenReturn(metadata);

        // Act
        // Assert
        mockMvc.perform(get("/api/v1/finances/currencies/{code}/metadata", "COP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COP"))
                .andExpect(jsonPath("$.name").value("Colombian peso"))
                .andExpect(jsonPath("$.symbol").value("$"))
                .andExpect(jsonPath("$.countries[0]").value("Colombia"));
    }

    @Test
    void shouldReturn400BadRequestWhenFeignNotFoundExceptionReachesHandler() throws Exception {
        // Arrange
        Request request = Request.create(Request.HttpMethod.GET, "/currency/XYZ", Collections.emptyMap(), null, new RequestTemplate());
        FeignException.NotFound notFound = new FeignException.NotFound("Not Found", request, null, Collections.emptyMap());
        when(divisasUseCase.consultarMetadatosMoneda("XYZ")).thenThrow(notFound);

        // Act
        // Assert
        mockMvc.perform(get("/api/v1/finances/currencies/{code}/metadata", "XYZ"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Moneda no soportada por el proveedor externo"));
    }

    @Test
    void shouldReturn502BadGatewayWhenGenericFeignExceptionReachesHandler() throws Exception {
        // Arrange
        Request request = Request.create(Request.HttpMethod.GET, "/currency/COP", Collections.emptyMap(), null, new RequestTemplate());
        FeignException.ServiceUnavailable unavailable = new FeignException.ServiceUnavailable(
                "Unavailable",
                request,
                "down".getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );
        when(divisasUseCase.consultarMetadatosMoneda("COP")).thenThrow(unavailable);

        // Act
        // Assert
        mockMvc.perform(get("/api/v1/finances/currencies/{code}/metadata", "COP"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("Proveedor externo de divisas temporalmente no disponible"));
    }

    @Test
    void shouldReturn409ConflictWhenIllegalStateExceptionOccurs() throws Exception {
        // Arrange
        when(proyeccionesUseCase.calcularBalanceNeto(userId))
                .thenThrow(new IllegalStateException("Conflicto en estado financiero"));

        // Act
        // Assert
        mockMvc.perform(get("/api/usuarios/balance"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Conflicto en estado financiero"));
    }

    @Test
    void shouldReturn400BadRequestWhenHttpMessageNotReadableExceptionOccurs() throws Exception {
        // Arrange
        when(proyeccionesUseCase.calcularBalanceNeto(userId))
                .thenThrow(new HttpMessageNotReadableException("Malformed JSON"));

        // Act
        // Assert
        mockMvc.perform(get("/api/usuarios/balance").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("El cuerpo de la solicitud JSON está malformado o es inválido"));
    }

    @Test
    void shouldReturn500InternalServerErrorWhenUnexpectedExceptionOccurs() throws Exception {
        // Arrange
        when(proyeccionesUseCase.calcularBalanceNeto(userId))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        // Assert
        mockMvc.perform(get("/api/usuarios/balance"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Ha ocurrido un error interno inesperado en el servidor"));
    }

    @Test
    void shouldReturn400BadRequestWhenJwtIsMissing() throws Exception {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act
        // Assert
        mockMvc.perform(get("/api/usuarios/balance"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
