package com.finance.transaction.infraestructure.driver_adapters.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.transaction.domain.model.VerifiedUserSession;
import com.finance.transaction.infraestructure.driver_adapters.feign.dto.AccountUserResponseDTO;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountFeignAdapterTest {

    @Mock
    private AccountFeignClient accountFeignClient;

    @InjectMocks
    private AccountFeignAdapter accountFeignAdapter;

    @Test
    void shouldVerifyLoggedUserAndReturnSessionWithIdFullNameRawTokenAndBaseCurrency() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        String bearerToken = "Bearer jwt-token-123";
        AccountUserResponseDTO responseDto = new AccountUserResponseDTO();
        responseDto.setId(userId);
        responseDto.setFullName("Esteban Morales");
        responseDto.setEmail("esteban@finance.com");
        responseDto.setIsActive(true);
        responseDto.setBaseCurrency(" cop ");

        when(accountFeignClient.getUserById(userId, "Bearer jwt-token-123")).thenReturn(responseDto);

        // Act
        VerifiedUserSession session = accountFeignAdapter.verifyLoggedUser(userId, bearerToken);

        // Assert
        assertNotNull(session);
        assertEquals(userId, session.id());
        assertEquals("Esteban Morales", session.fullName());
        assertEquals("jwt-token-123", session.token());
        assertEquals("COP", session.baseCurrency());
        assertEquals("COP", session.resolvedBaseCurrency());
        assertEquals("esteban@finance.com", responseDto.getEmail());
        verify(accountFeignClient).getUserById(userId, "Bearer jwt-token-123");
    }

    @Test
    void shouldNormalizeRawTokenWithoutBearerPrefixAndFallbackBaseCurrencyToUsdWhenBlank() throws Exception {
        // Arrange
        String userId = UUID.randomUUID().toString();
        String rawToken = "raw-jwt-token-456";
        AccountUserResponseDTO responseDto = new AccountUserResponseDTO(userId, "Esteban Morales", "esteban@finance.com", true);
        ObjectMapper mapper = new ObjectMapper();
        String jsonWithActiveAlias = "{\"id\":\"" + userId + "\",\"fullName\":\"Esteban\",\"active\":false,\"baseCurrency\":\"EUR\"}";
        when(accountFeignClient.getUserById(userId, "Bearer raw-jwt-token-456")).thenReturn(responseDto);

        // Act
        VerifiedUserSession session = accountFeignAdapter.verifyLoggedUser(" " + userId + " ", "  " + rawToken + "  ");
        AccountUserResponseDTO parsedAliasDto = mapper.readValue(jsonWithActiveAlias, AccountUserResponseDTO.class);

        // Assert
        assertEquals(userId, session.id());
        assertEquals("Esteban Morales", session.fullName());
        assertEquals(rawToken, session.token());
        assertEquals("USD", session.baseCurrency());
        assertFalse(parsedAliasDto.getIsActive());
        assertEquals("EUR", parsedAliasDto.getBaseCurrency());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenUserIdOrBearerTokenIsMissingOrBlank() {
        // Arrange
        String userId = UUID.randomUUID().toString();

        // Act
        IllegalArgumentException exNullUser = assertThrows(
                IllegalArgumentException.class,
                () -> accountFeignAdapter.verifyLoggedUser(null, "Bearer token")
        );
        IllegalArgumentException exBlankUser = assertThrows(
                IllegalArgumentException.class,
                () -> accountFeignAdapter.verifyLoggedUser("  ", "Bearer token")
        );
        IllegalArgumentException exNullToken = assertThrows(
                IllegalArgumentException.class,
                () -> accountFeignAdapter.verifyLoggedUser(userId, null)
        );
        IllegalArgumentException exBlankToken = assertThrows(
                IllegalArgumentException.class,
                () -> accountFeignAdapter.verifyLoggedUser(userId, "   ")
        );
        IllegalArgumentException exBearerOnly = assertThrows(
                IllegalArgumentException.class,
                () -> accountFeignAdapter.verifyLoggedUser(userId, "Bearer")
        );
        IllegalArgumentException exBearerSpaces = assertThrows(
                IllegalArgumentException.class,
                () -> accountFeignAdapter.verifyLoggedUser(userId, "Bearer    ")
        );

        // Assert
        assertEquals("El ID externo del usuario no puede ser nulo o vacío.", exNullUser.getMessage());
        assertEquals("El ID externo del usuario no puede ser nulo o vacío.", exBlankUser.getMessage());
        assertEquals("Token de sesión requerido", exNullToken.getMessage());
        assertEquals("Token de sesión requerido", exBlankToken.getMessage());
        assertEquals("Token de sesión requerido", exBearerOnly.getMessage());
        assertEquals("Token de sesión requerido", exBearerSpaces.getMessage());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenAccountResponseIsNullOrInactive() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        String token = "Bearer active-token";
        AccountUserResponseDTO inactiveResponse = new AccountUserResponseDTO(userId, "User", "u@f.com", false, "USD");

        when(accountFeignClient.getUserById(userId, token))
                .thenReturn(null)
                .thenReturn(inactiveResponse);

        // Act
        IllegalStateException exNullResponse = assertThrows(
                IllegalStateException.class,
                () -> accountFeignAdapter.verifyLoggedUser(userId, token)
        );
        IllegalStateException exInactive = assertThrows(
                IllegalStateException.class,
                () -> accountFeignAdapter.verifyLoggedUser(userId, token)
        );

        // Assert
        assertEquals("La sesión del usuario no pudo ser verificada", exNullResponse.getMessage());
        assertEquals("Cuenta desactivada. Reactiva tu cuenta para operar en transacciones y metas", exInactive.getMessage());
    }

    @Test
    void shouldTranslateFeignUnauthorizedForbiddenNotFoundAndRethrowGenericFeignException() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        String token = "Bearer test-token";
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/usuarios/get/" + userId,
                Collections.emptyMap(),
                null,
                new RequestTemplate()
        );
        FeignException.Unauthorized unauthorized = new FeignException.Unauthorized(
                "401", request, "unauthorized".getBytes(StandardCharsets.UTF_8), Collections.emptyMap()
        );
        FeignException.Forbidden forbidden = new FeignException.Forbidden(
                "403", request, "forbidden".getBytes(StandardCharsets.UTF_8), Collections.emptyMap()
        );
        FeignException.NotFound notFound = new FeignException.NotFound(
                "404", request, "not found".getBytes(StandardCharsets.UTF_8), Collections.emptyMap()
        );
        FeignException.ServiceUnavailable unavailable = new FeignException.ServiceUnavailable(
                "503", request, "down".getBytes(StandardCharsets.UTF_8), Collections.emptyMap()
        );

        when(accountFeignClient.getUserById(userId, token))
                .thenThrow(unauthorized)
                .thenThrow(forbidden)
                .thenThrow(notFound)
                .thenThrow(unavailable);

        // Act
        SecurityException ex401 = assertThrows(
                SecurityException.class,
                () -> accountFeignAdapter.verifyLoggedUser(userId, token)
        );
        AccessDeniedException ex403 = assertThrows(
                AccessDeniedException.class,
                () -> accountFeignAdapter.verifyLoggedUser(userId, token)
        );
        NoSuchElementException ex404 = assertThrows(
                NoSuchElementException.class,
                () -> accountFeignAdapter.verifyLoggedUser(userId, token)
        );
        FeignException ex503 = assertThrows(
                FeignException.class,
                () -> accountFeignAdapter.verifyLoggedUser(userId, token)
        );

        // Assert
        assertEquals("Sesión expirada o no iniciada en account-service", ex401.getMessage());
        assertEquals("No tiene permisos para operar con esta cuenta", ex403.getMessage());
        assertEquals("Usuario no encontrado en account-service", ex404.getMessage());
        assertEquals(503, ex503.status());
    }
}
