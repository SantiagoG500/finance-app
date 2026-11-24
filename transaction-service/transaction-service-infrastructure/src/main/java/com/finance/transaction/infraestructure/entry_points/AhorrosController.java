package com.finance.transaction.infraestructure.entry_points;

import com.finance.transaction.domain.model.MetaAhorro;
import com.finance.transaction.domain.model.PaginatedMetaAhorro;
import com.finance.transaction.domain.usecase.MetaAhorrosUseCase;
import com.finance.transaction.infraestructure.entry_points.dto.AbonoMetaRequestDTO;
import com.finance.transaction.infraestructure.entry_points.dto.CreateMetaAhorroRequestDTO;
import com.finance.transaction.infraestructure.entry_points.dto.MessageResponseDTO;
import com.finance.transaction.infraestructure.entry_points.dto.MetaAhorroResponseDTO;
import com.finance.transaction.infraestructure.entry_points.dto.UpdateMetaAhorroRequestDTO;
import com.finance.transaction.infraestructure.mapper.MetaAhorrosMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/finances/goals")
@Validated
@RequiredArgsConstructor
public class AhorrosController {

    private final MetaAhorrosUseCase metaAhorrosUseCase;
    private final MetaAhorrosMapper metaAhorrosMapper;

    @PostMapping("/save")
    public ResponseEntity<MetaAhorroResponseDTO> guardarMetaAhorro(
            @Valid @RequestBody CreateMetaAhorroRequestDTO request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String userId = JwtContextHelper.extractUserId(jwt);
        String bearerToken = JwtContextHelper.resolveBearerToken(authorizationHeader, jwt);
        MetaAhorro metaAhorro = metaAhorrosMapper.requestDtoToMetaAhorro(request, userId);
        MetaAhorro metaGuardada = metaAhorrosUseCase.guardarMetaAhorros(metaAhorro, bearerToken);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(metaAhorrosMapper.metaAhorroToResponseDto(metaGuardada));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<MetaAhorroResponseDTO> obtenerMetaAhorroPorId(
            @PathVariable("id") @NotBlank(message = "ID no debe estar vacío") String id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String userId = JwtContextHelper.extractUserId(jwt);
        String bearerToken = JwtContextHelper.resolveBearerToken(authorizationHeader, jwt);
        MetaAhorro metaAhorro = metaAhorrosUseCase.obtenerMetaAhorros(id, userId, bearerToken);
        if (metaAhorro == null) {
            throw new NoSuchElementException("Meta de ahorro no encontrada");
        }

        return ResponseEntity.ok(metaAhorrosMapper.metaAhorroToResponseDto(metaAhorro));
    }

    @GetMapping
    public ResponseEntity<PaginatedMetaAhorro> obtenerMetasPorUsuario(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String userId = JwtContextHelper.extractUserId(jwt);
        String bearerToken = JwtContextHelper.resolveBearerToken(authorizationHeader, jwt);
        PaginatedMetaAhorro metasPaginadas = metaAhorrosUseCase.obtenerMetasPaginadas(userId, bearerToken, page, size);

        return ResponseEntity.ok(metasPaginadas);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<MetaAhorroResponseDTO> actualizarMetaAhorro(
            @PathVariable("id") @NotBlank(message = "ID no debe estar vacío") String id,
            @Valid @RequestBody UpdateMetaAhorroRequestDTO request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String userId = JwtContextHelper.extractUserId(jwt);
        String bearerToken = JwtContextHelper.resolveBearerToken(authorizationHeader, jwt);
        MetaAhorro metaAhorro = metaAhorrosMapper.requestDtoToMetaAhorro(id, request, userId);
        MetaAhorro metaActualizada = metaAhorrosUseCase.actualizarMetaAhorros(metaAhorro, bearerToken);

        return ResponseEntity.ok(metaAhorrosMapper.metaAhorroToResponseDto(metaActualizada));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<MessageResponseDTO> eliminarMetaAhorro(
            @PathVariable("id") @NotBlank(message = "ID no debe estar vacío") String id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String userId = JwtContextHelper.extractUserId(jwt);
        String bearerToken = JwtContextHelper.resolveBearerToken(authorizationHeader, jwt);
        metaAhorrosUseCase.eliminarMetaAhorros(id, userId, bearerToken);
        return ResponseEntity.ok(new MessageResponseDTO("Meta de ahorro eliminada exitosamente"));
    }

    @PatchMapping("/{id}/deposit")
    public ResponseEntity<MetaAhorroResponseDTO> abonarMetaAhorro(
            @PathVariable("id") @NotBlank(message = "ID no debe estar vacío") String id,
            @Valid @RequestBody AbonoMetaRequestDTO request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String userId = JwtContextHelper.extractUserId(jwt);
        String userEmail = JwtContextHelper.extractUserEmail(jwt);
        String bearerToken = JwtContextHelper.resolveBearerToken(authorizationHeader, jwt);
        MetaAhorro metaActualizada = metaAhorrosUseCase.abonarMetaAhorros(id, userId, request.getAmount(), userEmail, bearerToken);
        return ResponseEntity.ok(metaAhorrosMapper.metaAhorroToResponseDto(metaActualizada));
    }
}
