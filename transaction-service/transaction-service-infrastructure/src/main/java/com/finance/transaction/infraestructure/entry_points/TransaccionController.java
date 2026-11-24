package com.finance.transaction.infraestructure.entry_points;

import com.finance.transaction.domain.model.Transaccion;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import com.finance.transaction.domain.usecase.TransaccionUseCase;
import com.finance.transaction.infraestructure.entry_points.dto.CreateTransaccionRequestDTO;
import com.finance.transaction.infraestructure.entry_points.dto.TransaccionResponseDTO;
import com.finance.transaction.infraestructure.mapper.TransaccionMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/finances")
@RequiredArgsConstructor
@Validated
public class TransaccionController {

    private final TransaccionUseCase transaccionUseCase;
    private final TransaccionMapper transaccionMapper;

    @PostMapping("/save")
    public ResponseEntity<TransaccionResponseDTO> guardarTransaccion(
            @Valid @RequestBody CreateTransaccionRequestDTO request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String userId = JwtContextHelper.extractUserId(jwt);
        String userEmail = JwtContextHelper.extractUserEmail(jwt);
        String bearerToken = JwtContextHelper.resolveBearerToken(authorizationHeader, jwt);
        Transaccion transaccion = transaccionMapper.requestDtoToTransaccion(request, userId);
        Transaccion transaccionCreada = transaccionUseCase.guardarTransaccion(transaccion, userEmail, bearerToken);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transaccionMapper.transaccionToResponseDto(transaccionCreada));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<TransaccionResponseDTO> obtenerTransaccionPorID(
            @PathVariable("id") @NotBlank(message = "ID no debe estar vacío") String id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String userId = JwtContextHelper.extractUserId(jwt);
        String bearerToken = JwtContextHelper.resolveBearerToken(authorizationHeader, jwt);
        Transaccion transaccion = transaccionUseCase.obtenerTransaccion(id, userId, bearerToken);
        if (transaccion == null) {
            throw new NoSuchElementException("Transacción no encontrada");
        }

        return ResponseEntity.ok(transaccionMapper.transaccionToResponseDto(transaccion));
    }

    @GetMapping("/transactions/by-type")
    public ResponseEntity<List<TransaccionResponseDTO>> obtenerTransaccionesPorTipo(
            @RequestParam("type") TransaccionType type,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String userId = JwtContextHelper.extractUserId(jwt);
        String bearerToken = JwtContextHelper.resolveBearerToken(authorizationHeader, jwt);
        List<Transaccion> transacciones = transaccionUseCase.obtenerTransaccionesPorTipo(userId, type, bearerToken);
        List<TransaccionResponseDTO> responseList = transacciones.stream()
                .map(transaccionMapper::transaccionToResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }
}
