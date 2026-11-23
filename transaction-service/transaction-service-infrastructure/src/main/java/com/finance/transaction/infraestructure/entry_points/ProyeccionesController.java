package com.finance.transaction.infraestructure.entry_points;

import com.finance.transaction.domain.model.ConvertedBalance;
import com.finance.transaction.domain.model.CurrencyMetadata;
import com.finance.transaction.domain.model.ResumenFinanciero;
import com.finance.transaction.domain.usecase.DivisasUseCase;
import com.finance.transaction.domain.usecase.ProyeccionesUseCase;
import com.finance.transaction.infraestructure.entry_points.dto.BalanceNetoResponseDTO;
import com.finance.transaction.infraestructure.entry_points.dto.ProyeccionSaldoResponseDTO;
import com.finance.transaction.infraestructure.entry_points.dto.ResumenFinancieroDTO;
import com.finance.transaction.infraestructure.mapper.ResumenFinancieroMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class ProyeccionesController {

    private final ProyeccionesUseCase proyeccionesUseCase;
    private final ResumenFinancieroMapper resumenFinancieroMapper;
    private final DivisasUseCase divisasUseCase;

    @GetMapping("/api/usuarios/summary")
    public ResponseEntity<ResumenFinancieroDTO> obtenerResumenFinanciero(
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = extractUserId(jwt);
        ResumenFinanciero resumen = proyeccionesUseCase.obtenerResumenFinanciero(userId);
        ResumenFinancieroDTO dto = resumenFinancieroMapper.resumenFinancieroToDto(resumen);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/api/usuarios/projections")
    public ResponseEntity<ProyeccionSaldoResponseDTO> proyectarSaldo(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "months", defaultValue = "6") int months
    ) {
        String userId = extractUserId(jwt);
        if (months < 1 || months > 120) {
            throw new IllegalArgumentException("El horizonte de meses debe estar entre 1 y 120");
        }

        String userEmail = extractUserEmail(jwt);
        BigDecimal saldoProyectado = proyeccionesUseCase.proyectarSaldo(userId, months, userEmail);
        ProyeccionSaldoResponseDTO response = ProyeccionSaldoResponseDTO.builder()
                .usuarioId(userId)
                .mesesProyectados(months)
                .saldoProyectado(saldoProyectado)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/usuarios/balance")
    public ResponseEntity<BalanceNetoResponseDTO> calcularBalanceNeto(
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = extractUserId(jwt);
        BigDecimal balanceNeto = proyeccionesUseCase.calcularBalanceNeto(userId);
        BalanceNetoResponseDTO response = BalanceNetoResponseDTO.builder()
                .usuarioId(userId)
                .balanceNetoMensual(balanceNeto)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/usuarios/balance/convert")
    public ResponseEntity<ConvertedBalance> convertirBalanceMensual(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "from", defaultValue = "USD") String from,
            @RequestParam(name = "to") String to
    ) {
        String userId = extractUserId(jwt);
        ConvertedBalance convertedBalance = divisasUseCase.convertirBalanceMensual(userId, from, to);
        return ResponseEntity.ok(convertedBalance);
    }

    @GetMapping("/api/v1/finances/currencies/{code}/metadata")
    public ResponseEntity<CurrencyMetadata> consultarMetadatosMoneda(
            @PathVariable("code") String code
    ) {
        CurrencyMetadata metadata = divisasUseCase.consultarMetadatosMoneda(code);
        return ResponseEntity.ok(metadata);
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
