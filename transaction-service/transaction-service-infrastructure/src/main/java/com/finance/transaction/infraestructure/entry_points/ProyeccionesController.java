package com.finance.transaction.infraestructure.entry_points;

import com.finance.transaction.domain.model.ResumenFinanciero;
import com.finance.transaction.domain.usecase.ProyeccionesUseCase;
import com.finance.transaction.infraestructure.entry_points.dto.ResumenFinancieroDTO;
import com.finance.transaction.infraestructure.mapper.ResumenFinancieroMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class ProyeccionesController {

    private final ProyeccionesUseCase proyeccionesUseCase;
    private final ResumenFinancieroMapper resumenFinancieroMapper;

    @GetMapping("/summary")
    public ResponseEntity<ResumenFinancieroDTO> obtenerResumenFinanciero(@AuthenticationPrincipal OAuth2User usuario) {
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String externalUserId = usuario.getAttribute("sub");
        if (externalUserId == null || externalUserId.trim().isEmpty()) {
            externalUserId = usuario.getName();
        }

        if (externalUserId == null || externalUserId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ResumenFinanciero resumen = proyeccionesUseCase.obtenerResumenFinanciero(externalUserId);
        ResumenFinancieroDTO dto = resumenFinancieroMapper.resumenFinancieroToDto(resumen);

        return ResponseEntity.ok(dto);
    }
}
