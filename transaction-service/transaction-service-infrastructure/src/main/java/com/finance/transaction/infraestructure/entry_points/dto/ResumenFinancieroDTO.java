package com.finance.transaction.infraestructure.entry_points.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResumenFinancieroDTO {

    private String usuarioId;
    private String moneda;
    private String monedaBase;
    private BigDecimal ingresosMensuales;
    private BigDecimal gastosMensuales;
    private BigDecimal balanceNetoMensual;
    private BigDecimal compromisoAhorroMensual;
    private BigDecimal margenLibre;

    private List<MetaAhorroResponseDTO> metasAhorro;

    private BigDecimal proyeccion1Ano;

    private Map<String, BigDecimal> porCategoria;

    public ResumenFinancieroDTO(
            String usuarioId,
            String moneda,
            BigDecimal ingresosMensuales,
            BigDecimal gastosMensuales,
            BigDecimal balanceNetoMensual,
            BigDecimal compromisoAhorroMensual,
            BigDecimal margenLibre,
            List<MetaAhorroResponseDTO> metasAhorro,
            BigDecimal proyeccion1Ano,
            Map<String, BigDecimal> porCategoria
    ) {
        this(
                usuarioId,
                moneda,
                moneda,
                ingresosMensuales,
                gastosMensuales,
                balanceNetoMensual,
                compromisoAhorroMensual,
                margenLibre,
                metasAhorro,
                proyeccion1Ano,
                porCategoria
        );
    }

}
