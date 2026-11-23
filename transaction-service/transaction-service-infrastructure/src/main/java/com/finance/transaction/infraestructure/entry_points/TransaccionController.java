package com.finance.transaction.infraestructure.entry_points;

import com.finance.transaction.domain.model.Transaccion;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import com.finance.transaction.domain.usecase.TransaccionUseCase;
import com.finance.transaction.infraestructure.entry_points.dto.MessageResponseDTO;
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
            @Valid @RequestBody TransaccionRequestDTO request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = extractUserId(jwt);
        String userEmail = extractUserEmail(jwt);
        Transaccion transaccion = transaccionMapper.requestDtoToTransaccion(request, userId);
        Transaccion transaccionCreada = transaccionUseCase.guardarTransaccion(transaccion, userEmail);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transaccionMapper.transaccionToResponseDto(transaccionCreada));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<TransaccionResponseDTO> obtenerTransaccionPorID(
            @PathVariable("id") @NotBlank(message = "ID no debe estar vacío") String id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = extractUserId(jwt);
        Transaccion transaccion = transaccionUseCase.obtenerTransaccion(id, userId);
        if (transaccion == null) {
            throw new NoSuchElementException("Transacción no encontrada");
        }

        return ResponseEntity.ok(transaccionMapper.transaccionToResponseDto(transaccion));
    }

    @PutMapping("/update")
    public ResponseEntity<TransaccionResponseDTO> actualizarTransaccion(
            @Valid @RequestBody TransaccionRequestDTO request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = extractUserId(jwt);
        Transaccion transaccion = transaccionMapper.requestDtoToTransaccion(request, userId);
        Transaccion transaccionActualizada = transaccionUseCase.actualizarTransaccion(transaccion);

        return ResponseEntity.ok(transaccionMapper.transaccionToResponseDto(transaccionActualizada));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<MessageResponseDTO> deleteTransaccion(
            @PathVariable("id") @NotBlank(message = "ID no debe estar vacío") String id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = extractUserId(jwt);
        transaccionUseCase.eliminarTransaccion(id, userId);
        return ResponseEntity.ok(new MessageResponseDTO("Transacción eliminada exitosamente"));
    }

    @GetMapping("/transactions/by-type")
    public ResponseEntity<List<TransaccionResponseDTO>> obtenerTransaccionesPorTipo(
            @RequestParam("type") TransaccionType type,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = extractUserId(jwt);
        List<Transaccion> transacciones = transaccionUseCase.obtenerTransaccionesPorTipo(userId, type);
        List<TransaccionResponseDTO> responseList = transacciones.stream()
                .map(transaccionMapper::transaccionToResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    private String extractUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("El token JWT no contiene un identificador de usuario válido");
        }
        return jwt.getSubject();
    }

    private String extractUserEmail(Jwt jwt) {
        if (jwt == null) {
            return null;
        }
        return jwt.getClaimAsString("email");
    }
}
