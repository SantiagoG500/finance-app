package com.finance.transaction.infraestructure.entry_points;

import com.finance.transaction.domain.model.MetaAhorro;
import com.finance.transaction.domain.usecase.MetaAhorrosUseCase;
import com.finance.transaction.infraestructure.entry_points.dto.MetaAhorroRequestDTO;
import com.finance.transaction.infraestructure.entry_points.dto.MetaAhorroResponseDTO;
import com.finance.transaction.infraestructure.mapper.MetaAhorrosMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
            @AuthenticationPrincipal OAuth2User usuario
    ) {
        String externalUserId = extractExternalUserId(usuario);
        if (externalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MetaAhorro metaAhorro = metaAhorrosMapper.requestDtoToMetaAhorro(request, externalUserId);
        MetaAhorro metaGuardada = metaAhorrosUseCase.guardarMetaAhorros(metaAhorro);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(metaAhorrosMapper.metaAhorroToResponseDto(metaGuardada));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<MetaAhorroResponseDTO> obtenerMetaAhorroPorId(
            @PathVariable("id") @NotBlank(message = "ID no debe estar vacío") String id,
            @AuthenticationPrincipal OAuth2User usuario
    ) {
        String externalUserId = extractExternalUserId(usuario);
        if (externalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MetaAhorro metaAhorro = metaAhorrosUseCase.obtenerMetaAhorros(id, externalUserId);
        if (metaAhorro == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(metaAhorrosMapper.metaAhorroToResponseDto(metaAhorro));
    }

    @GetMapping
    public ResponseEntity<List<MetaAhorroResponseDTO>> obtenerMetasPorUsuario(
            @AuthenticationPrincipal OAuth2User usuario
    ) {
        String externalUserId = extractExternalUserId(usuario);
        if (externalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<MetaAhorro> metas = metaAhorrosUseCase.obtenerMetasPorUsuario(externalUserId);
        List<MetaAhorroResponseDTO> responseList = metas.stream()
                .map(metaAhorrosMapper::metaAhorroToResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<MetaAhorroResponseDTO> actualizarMetaAhorro(
            @PathVariable("id") @NotBlank(message = "ID no debe estar vacío") String id,
            @Valid @RequestBody MetaAhorroRequestDTO request,
            @AuthenticationPrincipal OAuth2User usuario
    ) {
        String externalUserId = extractExternalUserId(usuario);
        if (externalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MetaAhorro metaAhorro = metaAhorrosMapper.requestDtoToMetaAhorro(id, request, externalUserId);
        MetaAhorro metaActualizada = metaAhorrosUseCase.actualizarMetaAhorros(metaAhorro);

        return ResponseEntity.ok(metaAhorrosMapper.metaAhorroToResponseDto(metaActualizada));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> eliminarMetaAhorro(
            @PathVariable("id") @NotBlank(message = "ID no debe estar vacío") String id,
            @AuthenticationPrincipal OAuth2User usuario
    ) {
        String externalUserId = extractExternalUserId(usuario);
        if (externalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        metaAhorrosUseCase.eliminarMetaAhorros(id, externalUserId);
        return ResponseEntity.noContent().build();
    }

    private String extractExternalUserId(OAuth2User usuario) {
        if (usuario == null) {
            return null;
        }
        String externalUserId = usuario.getAttribute("sub");
        if (externalUserId == null || externalUserId.trim().isEmpty()) {
            externalUserId = usuario.getName();
        }
        return (externalUserId != null && !externalUserId.trim().isEmpty()) ? externalUserId : null;
    }

}
