package com.finance.transaction.infraestructure.entry_points;

import com.finance.transaction.domain.model.ConvertedBalance;
import com.finance.transaction.domain.model.CurrencyMetadata;
import com.finance.transaction.domain.model.ResumenFinanciero;
import com.finance.transaction.domain.usecase.DivisasUseCase;
import com.finance.transaction.domain.usecase.ProyeccionesUseCase;
import com.finance.transaction.infraestructure.config.AsyncConfig;
import com.finance.transaction.infraestructure.config.CacheConfig;
import com.finance.transaction.infraestructure.entry_points.dto.ResumenFinancieroDTO;
import com.finance.transaction.infraestructure.entry_points.exception.GlobalExceptionHandler;
import com.finance.transaction.infraestructure.mapper.ResumenFinancieroMapper;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.RetryableException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
                .subject("user@finance.com")
                .claim("userId", userId)
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

        when(proyeccionesUseCase.obtenerResumenFinanciero(eq(userId), any())).thenReturn(resumen);
        when(resumenFinancieroMapper.resumenFinancieroToDto(resumen)).thenReturn(dto);

        // Act
        ResultActions response = mockMvc.perform(get("/api/v1/finances/summary")
                .header("Authorization", "Bearer mock-jwt-token"));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(userId))
                .andExpect(jsonPath("$.balanceNetoMensual").value(2000.00));
    }

    @Test
    void shouldProjectBalanceAndReturn200OkWhenMonthsIsInRange() throws Exception {
        // Arrange
        when(proyeccionesUseCase.proyectarSaldo(eq(userId), eq(12), eq("user@finance.com"), any()))
                .thenReturn(new BigDecimal("12000.00"));

        // Act
        ResultActions response = mockMvc.perform(get("/api/v1/finances/projections").param("months", "12"));

        // Assert
        response.andExpect(status().isOk())
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
        ResultActions belowRes = mockMvc.perform(get("/api/v1/finances/projections").param("months", belowRange));
        ResultActions aboveRes = mockMvc.perform(get("/api/v1/finances/projections").param("months", aboveRange));

        // Assert
        belowRes.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("El horizonte de meses debe estar entre 1 y 120"));
        aboveRes.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldGetNetBalanceAndReturn200Ok() throws Exception {
        // Arrange
        when(proyeccionesUseCase.calcularBalanceNeto(eq(userId), any())).thenReturn(new BigDecimal("1850.50"));

        // Act
        ResultActions response = mockMvc.perform(get("/api/v1/finances/balance"));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(userId))
                .andExpect(jsonPath("$.balanceNetoMensual").value(1850.50));
    }

    @Test
    void shouldConvertMonthlyBalanceWithRestCountriesMetadataAndReturn200Ok() throws Exception {
        // Arrange
        ConvertedBalance converted = new ConvertedBalance(
                userId,
                "USD",
                "United States dollar",
                "$",
                List.of("United States"),
                new BigDecimal("375.00"),
                1
        );
        when(divisasUseCase.convertirBalanceMensual(eq(userId), eq("USD"), any())).thenReturn(converted);

        // Act
        ResultActions response = mockMvc.perform(get("/api/v1/finances/balance/convert")
                .param("to", "USD"));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(userId))
                .andExpect(jsonPath("$.targetCurrency").value("USD"))
                .andExpect(jsonPath("$.targetCurrencyName").value("United States dollar"))
                .andExpect(jsonPath("$.targetCurrencySymbol").value("$"))
                .andExpect(jsonPath("$.targetCurrencyCountries[0]").value("United States"))
                .andExpect(jsonPath("$.convertedBalance").value(375.00))
                .andExpect(jsonPath("$.transactionsProcessed").value(1));
    }

    @Test
    void shouldGetCurrencyMetadataAndReturn200Ok() throws Exception {
        // Arrange
        CurrencyMetadata metadata = new CurrencyMetadata("COP", "Colombian peso", "$", List.of("Colombia"));
        when(divisasUseCase.consultarMetadatosMoneda("COP")).thenReturn(metadata);

        // Act
        ResultActions response = mockMvc.perform(get("/api/v1/finances/currencies/{code}/metadata", "COP"));

        // Assert
        response.andExpect(status().isOk())
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
        ResultActions response = mockMvc.perform(get("/api/v1/finances/currencies/{code}/metadata", "XYZ"));

        // Assert
        response.andExpect(status().isBadRequest())
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
        ResultActions response = mockMvc.perform(get("/api/v1/finances/currencies/{code}/metadata", "COP"));

        // Assert
        response.andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("Proveedor externo de divisas temporalmente no disponible"));
    }

    @Test
    void shouldReturn504GatewayTimeoutAnd503ServiceUnavailableWhenExternalTimeoutOrQueueSaturationOccurs() throws Exception {
        // Arrange
        Request request = Request.create(Request.HttpMethod.GET, "/latest/USD", Collections.emptyMap(), null, new RequestTemplate());
        RetryableException retryableException = new RetryableException(504, "Read timed out", Request.HttpMethod.GET, new Date(), request);
        when(divisasUseCase.consultarMetadatosMoneda("USD")).thenThrow(retryableException);
        when(divisasUseCase.consultarMetadatosMoneda("EUR")).thenThrow(new TaskRejectedException("Queue saturated"));

        // Act
        ResultActions timeoutRes = mockMvc.perform(get("/api/v1/finances/currencies/{code}/metadata", "USD"));
        ResultActions rejectedRes = mockMvc.perform(get("/api/v1/finances/currencies/{code}/metadata", "EUR"));

        // Assert
        timeoutRes.andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.status").value(504))
                .andExpect(jsonPath("$.message").value("Tiempo de espera agotado al comunicarse con un servicio externo"));
        rejectedRes.andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("El servidor está saturado temporalmente, intente nuevamente"));
    }

    @Test
    void shouldCreateBoundedNotificationAsyncExecutorAndCaffeineCacheManagerBeans() {
        // Arrange
        AsyncConfig asyncConfig = new AsyncConfig();
        CacheConfig cacheConfig = new CacheConfig();

        // Act
        Executor executor = asyncConfig.notificationAsyncExecutor();
        CacheManager cacheManager = cacheConfig.cacheManager();

        // Assert
        assertNotNull(executor);
        assertNotNull(cacheManager);
        assertNotNull(cacheManager.getCache("exchangeRates"));
        assertNotNull(cacheManager.getCache("currencyCodes"));
        assertNotNull(cacheManager.getCache("currencyMetadata"));
    }

    @Test
    void shouldReturn409ConflictWhenIllegalStateExceptionOccurs() throws Exception {
        // Arrange
        when(proyeccionesUseCase.calcularBalanceNeto(eq(userId), any()))
                .thenThrow(new IllegalStateException("Conflicto en estado financiero"));

        // Act
        ResultActions response = mockMvc.perform(get("/api/v1/finances/balance"));

        // Assert
        response.andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Conflicto en estado financiero"));
    }

    @Test
    void shouldReturn400BadRequestWhenHttpMessageNotReadableExceptionOccurs() throws Exception {
        // Arrange
        when(proyeccionesUseCase.calcularBalanceNeto(eq(userId), any()))
                .thenThrow(new HttpMessageNotReadableException("Malformed JSON"));

        // Act
        ResultActions response = mockMvc.perform(get("/api/v1/finances/balance").contentType(MediaType.APPLICATION_JSON));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("El cuerpo de la solicitud JSON está malformado o es inválido"));
    }

    @Test
    void shouldReturn500InternalServerErrorWhenUnexpectedExceptionOccurs() throws Exception {
        // Arrange
        when(proyeccionesUseCase.calcularBalanceNeto(eq(userId), any()))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResultActions response = mockMvc.perform(get("/api/v1/finances/balance"));

        // Assert
        response.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Ha ocurrido un error interno inesperado en el servidor"));
    }

    @Test
    void shouldReturn400BadRequestWhenJwtIsMissing() throws Exception {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act
        ResultActions response = mockMvc.perform(get("/api/v1/finances/balance"));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
