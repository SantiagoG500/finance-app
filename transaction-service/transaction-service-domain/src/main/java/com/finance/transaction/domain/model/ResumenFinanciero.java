package com.finance.transaction.domain.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ResumenFinanciero {
    private String usuarioId;
    private String moneda;
    private BigDecimal ingresosMensuales;
    private BigDecimal gastosMensuales;
    private BigDecimal balanceNetoMensual;
    private BigDecimal compromisoAhorroMensual;
    private BigDecimal margenLibre;
    private List<MetaAhorro> metasAhorro;
    private BigDecimal proyeccion1Ano;
}