package com.finance.transaction.infraestructure.entry_points.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResumenFinancieroDTO {

    private String usuarioId;
    private String moneda;
    private BigDecimal ingresosMensuales;
    private BigDecimal gastosMensuales;
    private BigDecimal balanceNetoMensual;
    private BigDecimal compromisoAhorroMensual;
    private BigDecimal margenLibre;

    private List<MetaAhorroResponseDTO> metasAhorro;

    private BigDecimal proyeccion1Ano;

}
