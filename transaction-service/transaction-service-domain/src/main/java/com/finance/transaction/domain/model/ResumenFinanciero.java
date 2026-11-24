package com.finance.transaction.domain.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ResumenFinanciero {
    private String usuarioId;
    private String moneda;
    private String monedaBase;
    private BigDecimal ingresosMensuales;
    private BigDecimal gastosMensuales;
    private BigDecimal balanceNetoMensual;
    private BigDecimal compromisoAhorroMensual;
    private BigDecimal margenLibre;
    private List<MetaAhorro> metasAhorro;
    private BigDecimal proyeccion1Ano;
    private Map<String, BigDecimal> porCategoria;
}
