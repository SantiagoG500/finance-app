package com.finance.transaction.infraestructure.driver_adapters.jpa_repository.adapter;

import com.finance.transaction.domain.model.Transaccion;
import com.finance.transaction.domain.model.enumns.FrecuenciaType;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import com.finance.transaction.domain.model.gateway.AccountVerificationGateway;
import com.finance.transaction.domain.model.gateway.CategoriaGateway;
import com.finance.transaction.domain.model.gateway.CurrencyMetadataGateway;
import com.finance.transaction.domain.model.gateway.ExchangeRateGateway;
import com.finance.transaction.domain.model.gateway.MetaAhorrosGateway;
import com.finance.transaction.domain.model.gateway.NotificationGateway;
import com.finance.transaction.infraestructure.config.SecurityConfig;
import com.finance.transaction.infraestructure.config.UseCaseConfig;
import com.finance.transaction.infraestructure.driver_adapters.feign.dto.ExchangeRateResponseDTO;
import com.finance.transaction.infraestructure.driver_adapters.feign.dto.RestCountryResponseDTO;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.CategoriaEntity;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.TransaccionEntity;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.repository.TransaccionJpaRepository;
import com.finance.transaction.infraestructure.entry_points.dto.BalanceNetoResponseDTO;
import com.finance.transaction.infraestructure.entry_points.dto.CreateTransaccionRequestDTO;
import com.finance.transaction.infraestructure.entry_points.dto.ErrorResponseDTO;
import com.finance.transaction.infraestructure.entry_points.dto.MessageResponseDTO;
import com.finance.transaction.infraestructure.entry_points.dto.MetaAhorroRequestDTO;
import com.finance.transaction.infraestructure.entry_points.dto.MetaAhorroResponseDTO;
import com.finance.transaction.infraestructure.entry_points.dto.ProyeccionSaldoResponseDTO;
import com.finance.transaction.infraestructure.entry_points.dto.ResumenFinancieroDTO;
import com.finance.transaction.infraestructure.entry_points.dto.TransaccionRequestDTO;
import com.finance.transaction.infraestructure.entry_points.dto.TransaccionResponseDTO;
import com.finance.transaction.infraestructure.entry_points.dto.UpdateMetaAhorroRequestDTO;
import com.finance.transaction.infraestructure.mapper.TransaccionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransaccionAdapterTest {

    @Mock
    private TransaccionJpaRepository jpaRepository;

    @Mock
    private TransaccionMapper mapper;

    @InjectMocks
    private TransaccionAdapter transaccionAdapter;

    @Test
    void shouldSaveTransactionSuccessfully() {
        // Arrange
        UUID txId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Transaccion domain = buildDomain(txId, userId, catId);
        TransaccionEntity entity = buildEntity(txId, userId, catId);

        when(mapper.transaccionToEntity(domain)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.entityToTransaccion(entity)).thenReturn(domain);

        // Act
        Transaccion saved = transaccionAdapter.guardarTransaccion(domain);

        // Assert
        assertNotNull(saved);
        assertEquals(txId.toString(), saved.getId());
        assertEquals(userId.toString(), saved.getExternalUserId());
        verify(jpaRepository).save(entity);
    }

    @Test
    void shouldGetTransactionWhenFoundAndReturnNullWhenMissing() {
        // Arrange
        UUID txId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Transaccion domain = buildDomain(txId, userId, catId);
        TransaccionEntity entity = buildEntity(txId, userId, catId);

        when(jpaRepository.findByIdAndUserId(txId, userId))
                .thenReturn(Optional.of(entity))
                .thenReturn(Optional.empty());
        when(mapper.entityToTransaccion(entity)).thenReturn(domain);

        // Act
        Transaccion found = transaccionAdapter.obtenerTransaccion(txId.toString(), userId.toString());
        Transaccion missing = transaccionAdapter.obtenerTransaccion(txId.toString(), userId.toString());

        // Assert
        assertNotNull(found);
        assertNull(missing);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenGetTransactionReceivesInvalidUuid() {
        // Arrange
        String invalidUuid = "not-a-uuid";
        String userId = UUID.randomUUID().toString();

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transaccionAdapter.obtenerTransaccion(invalidUuid, userId)
        );

        // Assert
        assertNotNull(exception.getMessage());
    }

    @Test
    void shouldListTransactionsByUserAndCheckExistenceByCategoryAndUserSuccessfully() {
        // Arrange
        UUID txId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Transaccion domain = buildDomain(txId, userId, catId);
        TransaccionEntity entity = buildEntity(txId, userId, catId);

        when(jpaRepository.findByUserId(userId)).thenReturn(List.of(entity));
        when(jpaRepository.existsByCategoryIdAndUserId(catId, userId)).thenReturn(true);
        when(mapper.entityToTransaccion(entity)).thenReturn(domain);

        // Act
        List<Transaccion> list = transaccionAdapter.obtenerTransaccionesPorUsuario(userId.toString());
        boolean existsByCat = transaccionAdapter.existeTransaccionPorCategoriaYUsuario(catId.toString(), userId.toString());

        // Assert
        assertEquals(1, list.size());
        assertEquals(txId.toString(), list.get(0).getId());
        assertEquals(userId.toString(), list.get(0).getExternalUserId());
        assertTrue(existsByCat);
        verify(jpaRepository).existsByCategoryIdAndUserId(catId, userId);
    }

    @Test
    void shouldMapBetweenEntityDomainAndDtosUsingRealTransaccionMapperAndCoverConfigAndDtos() {
        // Arrange
        TransaccionMapper realMapper = new TransaccionMapper();
        UUID txId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Transaccion domain = buildDomain(txId, userId, catId);
        CreateTransaccionRequestDTO createDto = new CreateTransaccionRequestDTO(
                catId.toString(),
                new BigDecimal("100.00"),
                "usd",
                TransaccionType.EXPENSE,
                FrecuenciaType.MONTHLY,
                "Desc",
                LocalDate.now()
        );
        TransaccionRequestDTO legacyDto = new TransaccionRequestDTO(
                txId.toString(),
                catId.toString(),
                new BigDecimal("100.00"),
                "USD",
                TransaccionType.EXPENSE,
                FrecuenciaType.MONTHLY,
                "Desc",
                LocalDate.now()
        );
        UseCaseConfig useCaseConfig = new UseCaseConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        ReflectionTestUtils.setField(securityConfig, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        CategoriaEntity categoriaEntity = new CategoriaEntity(catId, userId, "Food", TransaccionType.EXPENSE);
        RestCountryResponseDTO.CountryNameDTO countryNameDto = new RestCountryResponseDTO.CountryNameDTO("Colombia", "Republic of Colombia");
        RestCountryResponseDTO.CurrencyInfoDTO currencyInfoDto = new RestCountryResponseDTO.CurrencyInfoDTO("Colombian peso", "$");
        RestCountryResponseDTO restCountryDto = new RestCountryResponseDTO(countryNameDto, Map.of("COP", currencyInfoDto));
        ExchangeRateResponseDTO exchangeRateDto = new ExchangeRateResponseDTO("success", "USD", Map.of("COP", new BigDecimal("4000")));
        BalanceNetoResponseDTO balanceDto = new BalanceNetoResponseDTO(userId.toString(), new BigDecimal("100.00"));
        ProyeccionSaldoResponseDTO proyeccionDto = new ProyeccionSaldoResponseDTO(userId.toString(), 12, new BigDecimal("1200.00"));
        MessageResponseDTO msgDto = MessageResponseDTO.builder().message("ok").build();
        ErrorResponseDTO errDto = new ErrorResponseDTO("now", 400, "Bad Request", "msg", "/path");
        ResumenFinancieroDTO resumenDto = new ResumenFinancieroDTO(
                userId.toString(), "USD", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE,
                BigDecimal.ZERO, BigDecimal.ONE, Collections.emptyList(), BigDecimal.TEN, Map.of("Food", BigDecimal.ONE)
        );
        MetaAhorroRequestDTO metaReqDto = new MetaAhorroRequestDTO("Title", BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE, "USD", LocalDate.now());
        UpdateMetaAhorroRequestDTO updMetaDto = new UpdateMetaAhorroRequestDTO("Title", BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE, "USD", LocalDate.now());
        MetaAhorroResponseDTO metaResDto = new MetaAhorroResponseDTO("id", "Title", BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE, "USD", LocalDate.now(), LocalDateTime.now());

        // Act
        TransaccionEntity entity = realMapper.transaccionToEntity(domain);
        Transaccion backDomain = realMapper.entityToTransaccion(entity);
        Transaccion fromCreate = realMapper.requestDtoToTransaccion(createDto, userId.toString());
        Transaccion fromLegacy = realMapper.requestDtoToTransaccion(legacyDto, userId.toString());
        TransaccionResponseDTO responseDto = realMapper.transaccionToResponseDto(backDomain);

        // Assert
        assertNotNull(entity);
        assertEquals(txId, entity.getId());
        assertEquals(txId.toString(), backDomain.getId());
        assertEquals(userId.toString(), backDomain.getExternalUserId());
        assertEquals("USD", fromCreate.getCurrency());
        assertEquals(userId.toString(), fromLegacy.getExternalUserId());
        assertEquals(txId.toString(), responseDto.getId());
        assertEquals(userId.toString(), responseDto.getUserId());
        assertNull(realMapper.entityToTransaccion(null));
        assertNull(realMapper.transaccionToEntity(null));
        assertNull(realMapper.requestDtoToTransaccion((CreateTransaccionRequestDTO) null, userId.toString()));
        assertNull(realMapper.requestDtoToTransaccion((TransaccionRequestDTO) null, userId.toString()));
        assertNull(realMapper.transaccionToResponseDto(null));
        assertNotNull(useCaseConfig.proyeccionesUseCase(transaccionAdapter, mock(MetaAhorrosGateway.class), mock(NotificationGateway.class), mock(ExchangeRateGateway.class), mock(AccountVerificationGateway.class), mock(CategoriaGateway.class)));
        assertNotNull(useCaseConfig.transaccionUseCase(transaccionAdapter, mock(NotificationGateway.class), mock(AccountVerificationGateway.class), mock(CategoriaGateway.class)));
        assertNotNull(useCaseConfig.categoriaUseCase(mock(CategoriaGateway.class), transaccionAdapter, mock(AccountVerificationGateway.class)));
        assertNotNull(useCaseConfig.metaAhorrosUseCase(mock(MetaAhorrosGateway.class), mock(NotificationGateway.class), mock(AccountVerificationGateway.class)));
        assertNotNull(useCaseConfig.divisasUseCase(transaccionAdapter, mock(ExchangeRateGateway.class), mock(CurrencyMetadataGateway.class), mock(AccountVerificationGateway.class)));
        assertNotNull(securityConfig.corsConfigurationSource());
        assertNotNull(securityConfig.jwtDecoder());
        assertEquals("Food", categoriaEntity.getName());
        assertEquals("Colombia", restCountryDto.getName().getCommon());
        assertEquals("Colombian peso", restCountryDto.getCurrencies().get("COP").getName());
        assertEquals("USD", exchangeRateDto.getBaseCode());
        assertEquals(userId.toString(), balanceDto.getUsuarioId());
        assertEquals(12, proyeccionDto.getMesesProyectados());
        assertEquals("ok", msgDto.getMessage());
        assertEquals(400, errDto.getStatus());
        assertEquals("USD", resumenDto.getMoneda());
        assertEquals(BigDecimal.ONE, resumenDto.getPorCategoria().get("Food"));
        assertEquals("Title", metaReqDto.getTitle());
        assertEquals("Title", updMetaDto.getTitle());
        assertEquals("Title", metaResDto.getTitle());
    }

    private Transaccion buildDomain(UUID id, UUID userId, UUID categoryId) {
        return new Transaccion(
                id.toString(),
                userId.toString(),
                categoryId.toString(),
                new BigDecimal("250.00"),
                "USD",
                TransaccionType.INCOME,
                FrecuenciaType.MONTHLY,
                "Salario",
                LocalDate.now(),
                LocalDateTime.now()
        );
    }

    private TransaccionEntity buildEntity(UUID id, UUID userId, UUID categoryId) {
        return new TransaccionEntity(
                id,
                userId,
                categoryId,
                new BigDecimal("250.00"),
                "USD",
                TransaccionType.INCOME,
                FrecuenciaType.MONTHLY,
                "Salario",
                LocalDate.now(),
                LocalDateTime.now()
        );
    }
}
