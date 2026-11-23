package com.finance.transaction.infraestructure.entry_points;

import com.finance.transaction.domain.model.MetaAhorro;
import com.finance.transaction.domain.usecase.MetaAhorrosUseCase;
import com.finance.transaction.infraestructure.entry_points.dto.AbonoMetaRequestDTO;
import com.finance.transaction.infraestructure.entry_points.dto.MetaAhorroRequestDTO;
import com.finance.transaction.infraestructure.entry_points.dto.MetaAhorroResponseDTO;
import com.finance.transaction.infraestructure.mapper.MetaAhorrosMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/finances/goals")
@Validated
@RequiredArgsConstructor
public class AhorrosController {

    private final MetaAhorrosUseCase metaAhorrosUseCase;
    private final MetaAhorrosMapper metaAhorrosMapper;

    @PostMapping("/save")
    public ResponseEntity<MetaAhorroResponseDTO> guardarMetaAhorro(
            @Valid @RequestBody MetaAhorroRequestDTO request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = extractUserId(jwt);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MetaAhorro metaAhorro = metaAhorrosMapper.requestDtoToMetaAhorro(request, userId);
        MetaAhorro metaGuardada = metaAhorrosUseCase.guardarMetaAhorros(metaAhorro);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(metaAhorrosMapper.metaAhorroToResponseDto(metaGuardada));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<MetaAhorroResponseDTO> obtenerMetaAhorroPorId(
            @PathVariable("id") @NotBlank(message = "ID no debe estar vacío") String id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = extractUserId(jwt);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MetaAhorro metaAhorro = metaAhorrosUseCase.obtenerMetaAhorros(id, userId);
        if (metaAhorro == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(metaAhorrosMapper.metaAhorroToResponseDto(metaAhorro));
    }

    @GetMapping
    public ResponseEntity<List<MetaAhorroResponseDTO>> obtenerMetasPorUsuario(
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = extractUserId(jwt);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<MetaAhorro> metas = metaAhorrosUseCase.obtenerMetasPorUsuario(userId);
        List<MetaAhorroResponseDTO> responseList = metas.stream()
                .map(metaAhorrosMapper::metaAhorroToResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<MetaAhorroResponseDTO> actualizarMetaAhorro(
            @PathVariable("id") @NotBlank(message = "ID no debe estar vacío") String id,
            @Valid @RequestBody MetaAhorroRequestDTO request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = extractUserId(jwt);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MetaAhorro metaAhorro = metaAhorrosMapper.requestDtoToMetaAhorro(id, request, userId);
        MetaAhorro metaActualizada = metaAhorrosUseCase.actualizarMetaAhorros(metaAhorro);

        return ResponseEntity.ok(metaAhorrosMapper.metaAhorroToResponseDto(metaActualizada));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> eliminarMetaAhorro(
            @PathVariable("id") @NotBlank(message = "ID no debe estar vacío") String id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = extractUserId(jwt);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        metaAhorrosUseCase.eliminarMetaAhorros(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/deposit")
    public ResponseEntity<MetaAhorroResponseDTO> abonarMetaAhorro(
            @PathVariable("id") @NotBlank(message = "ID no debe estar vacío") String id,
            @Valid @RequestBody AbonoMetaRequestDTO request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = extractUserId(jwt);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MetaAhorro metaActualizada = metaAhorrosUseCase.abonarMetaAhorros(id, userId, request.getAmount());
        return ResponseEntity.ok(metaAhorrosMapper.metaAhorroToResponseDto(metaActualizada));
    }

    private String extractUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().trim().isEmpty()) {
            return null;
        }
        return jwt.getSubject();
    }
}
