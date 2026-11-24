package com.finance.transaction.infraestructure.driver_adapters.feign;

import com.finance.transaction.domain.model.VerifiedUserSession;
import com.finance.transaction.domain.model.gateway.AccountVerificationGateway;
import com.finance.transaction.infraestructure.driver_adapters.feign.dto.AccountUserResponseDTO;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountFeignAdapter implements AccountVerificationGateway {

    private final AccountFeignClient accountFeignClient;

    @Override
    public VerifiedUserSession verifyLoggedUser(String userId, String bearerToken) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("El ID externo del usuario no puede ser nulo o vacío.");
        }
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new IllegalArgumentException("Token de sesión requerido");
        }

        String trimmed = bearerToken.trim();
        String rawToken;
        String authHeader;
        if (trimmed.equalsIgnoreCase("Bearer")) {
            throw new IllegalArgumentException("Token de sesión requerido");
        } else if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            rawToken = trimmed.substring(7).trim();
            if (rawToken.isEmpty()) {
                throw new IllegalArgumentException("Token de sesión requerido");
            }
            authHeader = "Bearer " + rawToken;
        } else {
            rawToken = trimmed;
            authHeader = "Bearer " + rawToken;
        }

        try {
            AccountUserResponseDTO response = accountFeignClient.getUserById(userId.trim(), authHeader);
            if (response == null || !Boolean.TRUE.equals(response.getIsActive())) {
                throw new IllegalStateException("La cuenta de usuario está suspendida o inactiva");
            }
            return new VerifiedUserSession(
                    response.getId(),
                    response.getFullName(),
                    rawToken
            );
        } catch (FeignException.Unauthorized e) {
            log.warn("Sesión no autorizada o expirada en account-service para usuario {}: {}", userId, e.getMessage());
            throw new SecurityException("Sesión expirada o no iniciada en account-service");
        } catch (FeignException.Forbidden e) {
            log.warn("Acceso denegado en account-service para usuario {}: {}", userId, e.getMessage());
            throw new AccessDeniedException("No tiene permisos para operar con esta cuenta");
        } catch (FeignException.NotFound e) {
            log.warn("Usuario {} no encontrado en account-service: {}", userId, e.getMessage());
            throw new NoSuchElementException("Usuario no encontrado en account-service");
        } catch (FeignException e) {
            log.warn("Error de comunicación con account-service para usuario {}: {}", userId, e.getMessage());
            throw e;
        }
    }
}
