package com.finance.transaction.infraestructure.entry_points;

import com.finance.transaction.domain.model.Transaccion;
import com.finance.transaction.domain.usecase.TransaccionUseCase;
import com.finance.transaction.infraestructure.entry_points.dto.TransaccionRequestDTO;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/finances")
@RequiredArgsConstructor
@Validated
public class TransaccionController {

    private final TransaccionUseCase transaccionUseCase;
    private final TransaccionMapper transaccionMapper;

    @PostMapping("/save")
    public ResponseEntity<TransaccionResponseDTO> guardarTransaccion(
            @Valid @RequestBody TransaccionRequestDTO request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = extractUserId(jwt);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Transaccion transaccion = transaccionMapper.requestDtoToTransaccion(request, userId);
        Transaccion transaccionCreada = transaccionUseCase.guardarTransaccion(transaccion);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transaccionMapper.transaccionToResponseDto(transaccionCreada));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<TransaccionResponseDTO> obtenerTransaccionPorID(
            @PathVariable("id") @NotBlank(message = "ID no debe estar vacío") String id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = extractUserId(jwt);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Transaccion transaccion = transaccionUseCase.obtenerTransaccion(id, userId);
        if (transaccion == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(transaccionMapper.transaccionToResponseDto(transaccion));
    }

    @PutMapping("/update")
    public ResponseEntity<TransaccionResponseDTO> actualizarTransaccion(
            @Valid @RequestBody TransaccionRequestDTO request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = extractUserId(jwt);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Transaccion transaccion = transaccionMapper.requestDtoToTransaccion(request, userId);
        Transaccion transaccionActualizada = transaccionUseCase.actualizarTransaccion(transaccion);

        return ResponseEntity.ok(transaccionMapper.transaccionToResponseDto(transaccionActualizada));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTransaccion(
            @PathVariable("id") @NotBlank(message = "ID no debe estar vacío") String id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = extractUserId(jwt);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        transaccionUseCase.eliminarTransaccion(id, userId);
        return ResponseEntity.noContent().build();
    }

    private String extractUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().trim().isEmpty()) {
            return null;
        }
        return jwt.getSubject();
    }
}
