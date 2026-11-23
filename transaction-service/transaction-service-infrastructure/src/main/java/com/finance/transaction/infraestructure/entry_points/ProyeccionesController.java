package com.finance.transaction.infraestructure.entry_points;

import com.finance.transaction.domain.model.ResumenFinanciero;
import com.finance.transaction.domain.usecase.ProyeccionesUseCase;
import com.finance.transaction.infraestructure.entry_points.dto.BalanceNetoResponseDTO;
import com.finance.transaction.infraestructure.entry_points.dto.ProyeccionSaldoResponseDTO;
import com.finance.transaction.infraestructure.entry_points.dto.ResumenFinancieroDTO;
import com.finance.transaction.infraestructure.mapper.ResumenFinancieroMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class ProyeccionesController {

    private final ProyeccionesUseCase proyeccionesUseCase;
    private final ResumenFinancieroMapper resumenFinancieroMapper;

    @GetMapping("/summary")
    public ResponseEntity<ResumenFinancieroDTO> obtenerResumenFinanciero(
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = extractUserId(jwt);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ResumenFinanciero resumen = proyeccionesUseCase.obtenerResumenFinanciero(userId);
        ResumenFinancieroDTO dto = resumenFinancieroMapper.resumenFinancieroToDto(resumen);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/projections")
    public ResponseEntity<ProyeccionSaldoResponseDTO> proyectarSaldo(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "months", defaultValue = "6") int months
    ) {
        String userId = extractUserId(jwt);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (months < 1 || months > 120) {
            throw new IllegalArgumentException("El horizonte de meses debe estar entre 1 y 120");
        }

        BigDecimal saldoProyectado = proyeccionesUseCase.proyectarSaldo(userId, months);
        ProyeccionSaldoResponseDTO response = ProyeccionSaldoResponseDTO.builder()
                .usuarioId(userId)
                .mesesProyectados(months)
                .saldoProyectado(saldoProyectado)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceNetoResponseDTO> calcularBalanceNeto(
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = extractUserId(jwt);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        BigDecimal balanceNeto = proyeccionesUseCase.calcularBalanceNeto(userId);
        BalanceNetoResponseDTO response = BalanceNetoResponseDTO.builder()
                .usuarioId(userId)
                .balanceNetoMensual(balanceNeto)
                .build();

        return ResponseEntity.ok(response);
    }

    private String extractUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().trim().isEmpty()) {
            return null;
        }
        return jwt.getSubject();
    }
}
