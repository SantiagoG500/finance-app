package com.finance.transaction.domain.usecase;

import com.finance.transaction.domain.model.MetaAhorro;
import com.finance.transaction.domain.model.ResumenFinanciero;
import com.finance.transaction.domain.model.Transaccion;
import com.finance.transaction.domain.model.enumns.FrecuenciaType;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import com.finance.transaction.domain.model.gateway.MetaAhorrosGateway;
import com.finance.transaction.domain.model.gateway.TransaccionGateway;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class ProyeccionesUseCase {

    private final TransaccionGateway tGateway;
    private final MetaAhorrosGateway mGateway;

    public static final BigDecimal DIAS_POR_MES = new BigDecimal("30.41");
    public static final BigDecimal SEMANAS_POR_MES = new BigDecimal("4.33");
    public static final BigDecimal MESES_POR_ANO = new BigDecimal("12");
    public static final int DECIMAL_SCALE = 2;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    // 1. Normalización Mensual (Monthly Normalization)
    public BigDecimal normalizarMontoMensual(BigDecimal monto, FrecuenciaType frecuencia) {
        if (monto == null || frecuencia == null) {
            return BigDecimal.ZERO.setScale(DECIMAL_SCALE, ROUNDING_MODE);
        }

        switch (frecuencia) {
            case DAILY:
                return monto.multiply(DIAS_POR_MES).setScale(DECIMAL_SCALE, ROUNDING_MODE);
            case WEEKLY:
                return monto.multiply(SEMANAS_POR_MES).setScale(DECIMAL_SCALE, ROUNDING_MODE);
            case MONTHLY:
                return monto.setScale(DECIMAL_SCALE, ROUNDING_MODE);
            case YEARLY:
                return monto.divide(MESES_POR_ANO, DECIMAL_SCALE, ROUNDING_MODE);
            case ONE_TIME:
            default:
                return BigDecimal.ZERO.setScale(DECIMAL_SCALE, ROUNDING_MODE);
        }
    }

    public BigDecimal normalizarMontoMensual(BigDecimal monto, String frecuencia) {
        if (frecuencia == null || frecuencia.trim().isEmpty()) {
            return BigDecimal.ZERO.setScale(DECIMAL_SCALE, ROUNDING_MODE);
        }
        try {
            FrecuenciaType tipo = FrecuenciaType.valueOf(frecuencia.trim().toUpperCase());
            return normalizarMontoMensual(monto, tipo);
        } catch (IllegalArgumentException e) {
            return BigDecimal.ZERO.setScale(DECIMAL_SCALE, ROUNDING_MODE);
        }
    }

    // 2. Consolidación de Balance (Balance Consolidation)
    public BigDecimal calcularIngresosMensuales(List<Transaccion> transacciones) {
        if (transacciones == null || transacciones.isEmpty()) {
            return BigDecimal.ZERO.setScale(DECIMAL_SCALE, ROUNDING_MODE);
        }
        return transacciones.stream()
                .filter(t -> t.getType() == TransaccionType.INCOME)
                .map(t -> normalizarMontoMensual(t.getAmount(), t.getFrequency()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }

    public BigDecimal calcularGastosMensuales(List<Transaccion> transacciones) {
        if (transacciones == null || transacciones.isEmpty()) {
            return BigDecimal.ZERO.setScale(DECIMAL_SCALE, ROUNDING_MODE);
        }
        return transacciones.stream()
                .filter(t -> t.getType() == TransaccionType.EXPENSE)
                .map(t -> normalizarMontoMensual(t.getAmount(), t.getFrequency()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }

    public BigDecimal calcularBalanceNeto(String externalUserId) {
        validarExternalUserId(externalUserId);
        List<Transaccion> transacciones = tGateway.obtenerTransaccionesPorUsuario(externalUserId);
        return calcularBalanceNeto(transacciones);
    }

    public BigDecimal calcularBalanceNeto(List<Transaccion> transacciones) {
        BigDecimal ingresos = calcularIngresosMensuales(transacciones);
        BigDecimal gastos = calcularGastosMensuales(transacciones);
        return ingresos.subtract(gastos).setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }

    public BigDecimal calcularCompromisoAhorroMensual(List<MetaAhorro> metas) {
        if (metas == null || metas.isEmpty()) {
            return BigDecimal.ZERO.setScale(DECIMAL_SCALE, ROUNDING_MODE);
        }
        return metas.stream()
                .map(MetaAhorro::getMonthlyContribution)
                .filter(contribution -> contribution != null && contribution.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }

    public BigDecimal calcularSaldoInicialAcumulado(List<Transaccion> transacciones) {
        if (transacciones == null || transacciones.isEmpty()) {
            return BigDecimal.ZERO.setScale(DECIMAL_SCALE, ROUNDING_MODE);
        }
        return transacciones.stream()
                .filter(t -> t.getFrequency() == FrecuenciaType.ONE_TIME && t.getAmount() != null)
                .map(t -> t.getType() == TransaccionType.INCOME ? t.getAmount() : t.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }

    // 3. Proyección a n Meses (Projections)
    public BigDecimal proyectarSaldo(String externalUserId, int meses) {
        validarExternalUserId(externalUserId);
        List<Transaccion> transacciones = tGateway.obtenerTransaccionesPorUsuario(externalUserId);
        BigDecimal saldoInicial = calcularSaldoInicialAcumulado(transacciones);
        BigDecimal balanceNeto = calcularBalanceNeto(transacciones);
        return proyectarSaldo(saldoInicial, balanceNeto, meses);
    }

    public BigDecimal proyectarSaldo(BigDecimal saldoInicial, BigDecimal balanceNeto, int meses) {
        BigDecimal saldo = saldoInicial != null ? saldoInicial : BigDecimal.ZERO;
        BigDecimal balance = balanceNeto != null ? balanceNeto : BigDecimal.ZERO;
        BigDecimal mesesBD = BigDecimal.valueOf(meses);
        return saldo.add(balance.multiply(mesesBD)).setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }

    public ResumenFinanciero obtenerResumenFinanciero(String externalUserId) {
        validarExternalUserId(externalUserId);
        return proyectarSaldo(externalUserId);
    }

    public ResumenFinanciero proyectarSaldo(String externalUserId) {
        validarExternalUserId(externalUserId);
        List<Transaccion> transaccionesFromGateway = tGateway.obtenerTransaccionesPorUsuario(externalUserId);
        final List<Transaccion> transacciones = transaccionesFromGateway != null ? transaccionesFromGateway : Collections.emptyList();

        List<MetaAhorro> metasFromGateway = mGateway.obtenerMetasPorUsuario(externalUserId);
        final List<MetaAhorro> metas = metasFromGateway != null ? metasFromGateway : Collections.emptyList();

        BigDecimal ingresosMensuales = calcularIngresosMensuales(transacciones);
        BigDecimal gastosMensuales = calcularGastosMensuales(transacciones);
        BigDecimal balanceNetoMensual = ingresosMensuales.subtract(gastosMensuales).setScale(DECIMAL_SCALE, ROUNDING_MODE);
        BigDecimal compromisoAhorroMensual = calcularCompromisoAhorroMensual(metas);
        BigDecimal margenLibre = balanceNetoMensual.subtract(compromisoAhorroMensual).setScale(DECIMAL_SCALE, ROUNDING_MODE);

        BigDecimal saldoInicial = calcularSaldoInicialAcumulado(transacciones);
        BigDecimal proyeccion1Ano = proyectarSaldo(saldoInicial, balanceNetoMensual, 12);

        String moneda = transacciones.stream()
                .map(Transaccion::getCurrency)
                .filter(c -> c != null && !c.trim().isEmpty())
                .findFirst()
                .orElseGet(() -> metas.stream()
                        .map(MetaAhorro::getCurrency)
                        .filter(c -> c != null && !c.trim().isEmpty())
                        .findFirst()
                        .orElse("USD"));

        ResumenFinanciero resumen = new ResumenFinanciero();
        resumen.setUsuarioId(externalUserId);
        resumen.setMoneda(moneda);
        resumen.setIngresosMensuales(ingresosMensuales);
        resumen.setGastosMensuales(gastosMensuales);
        resumen.setBalanceNetoMensual(balanceNetoMensual);
        resumen.setCompromisoAhorroMensual(compromisoAhorroMensual);
        resumen.setMargenLibre(margenLibre);
        resumen.setMetasAhorro(metas);
        resumen.setProyeccion1Ano(proyeccion1Ano);

        return resumen;
    }

    private void validarExternalUserId(String externalUserId) {
        if (externalUserId == null || externalUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID externo del usuario no puede ser nulo o vacío.");
        }
    }
}
