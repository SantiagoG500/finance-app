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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/finances")
@RequiredArgsConstructor
public class ProyeccionesController {

    private final ProyeccionesUseCase proyeccionesUseCase;
    private final ResumenFinancieroMapper resumenFinancieroMapper;
    private final DivisasUseCase divisasUseCase;

    @GetMapping("/summary")
    public ResponseEntity<ResumenFinancieroDTO> obtenerResumenFinanciero(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String userId = JwtContextHelper.extractUserId(jwt);
        String bearerToken = JwtContextHelper.resolveBearerToken(authorizationHeader, jwt);
        ResumenFinanciero resumen = proyeccionesUseCase.obtenerResumenFinanciero(userId, bearerToken);
        ResumenFinancieroDTO dto = resumenFinancieroMapper.resumenFinancieroToDto(resumen);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/projections")
    public ResponseEntity<ProyeccionSaldoResponseDTO> proyectarSaldo(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "months", defaultValue = "6") int months,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String userId = JwtContextHelper.extractUserId(jwt);
        if (months < 1 || months > 120) {
            throw new IllegalArgumentException("El horizonte de meses debe estar entre 1 y 120");
        }

        String userEmail = JwtContextHelper.extractUserEmail(jwt);
        String bearerToken = JwtContextHelper.resolveBearerToken(authorizationHeader, jwt);
        BigDecimal saldoProyectado = proyeccionesUseCase.proyectarSaldo(userId, months, userEmail, bearerToken);
        ProyeccionSaldoResponseDTO response = ProyeccionSaldoResponseDTO.builder()
                .usuarioId(userId)
                .mesesProyectados(months)
                .saldoProyectado(saldoProyectado)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceNetoResponseDTO> calcularBalanceNeto(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String userId = JwtContextHelper.extractUserId(jwt);
        String bearerToken = JwtContextHelper.resolveBearerToken(authorizationHeader, jwt);
        BigDecimal balanceNeto = proyeccionesUseCase.calcularBalanceNeto(userId, bearerToken);
        BalanceNetoResponseDTO response = BalanceNetoResponseDTO.builder()
                .usuarioId(userId)
                .balanceNetoMensual(balanceNeto)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance/convert")
    public ResponseEntity<ConvertedBalance> convertirBalanceMensual(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "to") String to,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String userId = JwtContextHelper.extractUserId(jwt);
        String bearerToken = JwtContextHelper.resolveBearerToken(authorizationHeader, jwt);
        ConvertedBalance convertedBalance = divisasUseCase.convertirBalanceMensual(userId, to, bearerToken);
        return ResponseEntity.ok(convertedBalance);
    }

    @GetMapping("/currencies/{code}/metadata")
    public ResponseEntity<CurrencyMetadata> consultarMetadatosMoneda(
            @PathVariable("code") String code
    ) {
        CurrencyMetadata metadata = divisasUseCase.consultarMetadatosMoneda(code);
        return ResponseEntity.ok(metadata);
    }
}
