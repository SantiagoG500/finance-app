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
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/finances")
@RequiredArgsConstructor
@Validated
public class TransaccionController {

    private final TransaccionUseCase transaccionUseCase;
    private final TransaccionMapper transaccionMapper;

    @PostMapping("/save")
    public ResponseEntity<TransaccionResponseDTO> guardarTransaccion(
           @Valid @RequestBody TransaccionRequestDTO request,
           @AuthenticationPrincipal OAuth2User usuario
    ) {
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String externalUserId = usuario.getAttribute("sub");
        if (externalUserId == null || externalUserId.trim().isEmpty()) {
            externalUserId = usuario.getName();
        }

        Transaccion transaccion = transaccionMapper.requestDtoToTransaccion(request, externalUserId);
        Transaccion transaccionCreada = transaccionUseCase.guardarTransaccion(transaccion);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transaccionMapper.transaccionToResponseDto(transaccionCreada));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<TransaccionResponseDTO> obtenerTransaccionPorID(
            @PathVariable("id") @NotBlank(message = "ID no debe estar vacío") String id,
            @AuthenticationPrincipal OAuth2User usuario
    ) {
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String externalUserId = usuario.getAttribute("sub");
        if (externalUserId == null || externalUserId.trim().isEmpty()) {
            externalUserId = usuario.getName();
        }

        Transaccion transaccion = transaccionUseCase.obtenerTransaccion(id, externalUserId);
        if (transaccion == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(transaccionMapper.transaccionToResponseDto(transaccion));
    }

    @PutMapping("/update")
    public ResponseEntity<TransaccionResponseDTO> actualizarTransaccion(
            @Valid @RequestBody TransaccionRequestDTO request,
            @AuthenticationPrincipal OAuth2User usuario
    ) {
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String externalUserId = usuario.getAttribute("sub");
        if (externalUserId == null || externalUserId.trim().isEmpty()) {
            externalUserId = usuario.getName();
        }

        Transaccion transaccion = transaccionMapper.requestDtoToTransaccion(request, externalUserId);
        Transaccion transaccionActualizada = transaccionUseCase.actualizarTransaccion(transaccion);

        return ResponseEntity.ok(transaccionMapper.transaccionToResponseDto(transaccionActualizada));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTransaccion(
            @PathVariable("id") @NotBlank(message = "ID no debe estar vacío") String id,
            @AuthenticationPrincipal OAuth2User usuario
    ) {
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String externalUserId = usuario.getAttribute("sub");
        if (externalUserId == null || externalUserId.trim().isEmpty()) {
            externalUserId = usuario.getName();
        }

        transaccionUseCase.eliminarTransaccion(id, externalUserId);
        return ResponseEntity.noContent().build();
    }

}
