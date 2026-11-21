package com.finance.transaction.infraestructure.entry_points;

import com.finance.transaction.domain.model.ResumenFinanciero;
import com.finance.transaction.domain.usecase.ProyeccionesUseCase;
import com.finance.transaction.infraestructure.entry_points.dto.ResumenFinancieroDTO;
import com.finance.transaction.infraestructure.mapper.ResumenFinancieroMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    private String extractUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().trim().isEmpty()) {
            return null;
        }
        return jwt.getSubject();
    }
}
