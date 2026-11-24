package com.finance.transaction.infraestructure.entry_points.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProyeccionSaldoResponseDTO {

    private String usuarioId;
    private int mesesProyectados;
    private BigDecimal saldoProyectado;

}
