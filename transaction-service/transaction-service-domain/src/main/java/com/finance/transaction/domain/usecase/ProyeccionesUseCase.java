package com.finance.transaction.domain.usecase;

import com.finance.transaction.domain.model.Categoria;
import com.finance.transaction.domain.model.MetaAhorro;
import com.finance.transaction.domain.model.ResumenFinanciero;
import com.finance.transaction.domain.model.Transaccion;
import com.finance.transaction.domain.model.VerifiedUserSession;
import com.finance.transaction.domain.model.enumns.FrecuenciaType;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import com.finance.transaction.domain.model.gateway.AccountVerificationGateway;
import com.finance.transaction.domain.model.gateway.CategoriaGateway;
import com.finance.transaction.domain.model.gateway.ExchangeRateGateway;
import com.finance.transaction.domain.model.gateway.MetaAhorrosGateway;
import com.finance.transaction.domain.model.gateway.NotificationGateway;
import com.finance.transaction.domain.model.gateway.TransaccionGateway;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProyeccionesUseCase {

    private final TransaccionGateway tGateway;
    private final MetaAhorrosGateway mGateway;
    private final NotificationGateway notificationGateway;
    private final ExchangeRateGateway exchangeRateGateway;
    private final AccountVerificationGateway accountVerificationGateway;
    private final CategoriaGateway categoriaGateway;

    public ProyeccionesUseCase(TransaccionGateway tGateway, MetaAhorrosGateway mGateway) {
        this(tGateway, mGateway, null, null, null, null);
    }

    public ProyeccionesUseCase(
            TransaccionGateway tGateway,
            MetaAhorrosGateway mGateway,
            NotificationGateway notificationGateway
    ) {
        this(tGateway, mGateway, notificationGateway, null, null, null);
    }

    public ProyeccionesUseCase(
            TransaccionGateway tGateway,
            MetaAhorrosGateway mGateway,
            NotificationGateway notificationGateway,
            ExchangeRateGateway exchangeRateGateway
    ) {
        this(tGateway, mGateway, notificationGateway, exchangeRateGateway, null, null);
    }

    public ProyeccionesUseCase(
            TransaccionGateway tGateway,
            MetaAhorrosGateway mGateway,
            NotificationGateway notificationGateway,
            ExchangeRateGateway exchangeRateGateway,
            AccountVerificationGateway accountVerificationGateway
    ) {
        this(tGateway, mGateway, notificationGateway, exchangeRateGateway, accountVerificationGateway, null);
    }

    public ProyeccionesUseCase(
            TransaccionGateway tGateway,
            MetaAhorrosGateway mGateway,
            NotificationGateway notificationGateway,
            ExchangeRateGateway exchangeRateGateway,
            AccountVerificationGateway accountVerificationGateway,
            CategoriaGateway categoriaGateway
    ) {
        this.tGateway = tGateway;
        this.mGateway = mGateway;
        this.notificationGateway = notificationGateway;
        this.exchangeRateGateway = exchangeRateGateway;
        this.accountVerificationGateway = accountVerificationGateway;
        this.categoriaGateway = categoriaGateway;
    }

    public static final BigDecimal DIAS_POR_MES = new BigDecimal("30.41");
    public static final BigDecimal SEMANAS_POR_MES = new BigDecimal("4.33");
    public static final BigDecimal MESES_POR_ANO = new BigDecimal("12");
    public static final int DECIMAL_SCALE = 2;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    public static final String CATEGORIA_SIN_ASIGNAR = "Sin categoría";

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
        String referenceCurrency = resolveReferenceCurrency(transacciones, null);
        Map<String, BigDecimal> ratesCache = precalcularTasasDeCambio(transacciones, null, referenceCurrency);
        return calcularIngresosMensuales(transacciones, referenceCurrency, ratesCache);
    }

    private BigDecimal calcularIngresosMensuales(
            List<Transaccion> transacciones,
            String referenceCurrency,
            Map<String, BigDecimal> ratesCache
    ) {
        if (transacciones == null || transacciones.isEmpty()) {
            return BigDecimal.ZERO.setScale(DECIMAL_SCALE, ROUNDING_MODE);
        }
        return transacciones.stream()
                .filter(t -> t != null && t.getType() == TransaccionType.INCOME)
                .map(t -> convertirMontoAMonedaReferencia(
                        normalizarMontoMensual(t.getAmount(), t.getFrequency()),
                        t.getCurrency(),
                        referenceCurrency,
                        ratesCache
                ))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }

    public BigDecimal calcularGastosMensuales(List<Transaccion> transacciones) {
        if (transacciones == null || transacciones.isEmpty()) {
            return BigDecimal.ZERO.setScale(DECIMAL_SCALE, ROUNDING_MODE);
        }
        String referenceCurrency = resolveReferenceCurrency(transacciones, null);
        Map<String, BigDecimal> ratesCache = precalcularTasasDeCambio(transacciones, null, referenceCurrency);
        return calcularGastosMensuales(transacciones, referenceCurrency, ratesCache);
    }

    private BigDecimal calcularGastosMensuales(
            List<Transaccion> transacciones,
            String referenceCurrency,
            Map<String, BigDecimal> ratesCache
    ) {
        if (transacciones == null || transacciones.isEmpty()) {
            return BigDecimal.ZERO.setScale(DECIMAL_SCALE, ROUNDING_MODE);
        }
        return transacciones.stream()
                .filter(t -> t != null && t.getType() == TransaccionType.EXPENSE)
                .map(t -> convertirMontoAMonedaReferencia(
                        normalizarMontoMensual(t.getAmount(), t.getFrequency()),
                        t.getCurrency(),
                        referenceCurrency,
                        ratesCache
                ))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }

    public BigDecimal calcularBalanceNeto(String externalUserId) {
        return calcularBalanceNeto(externalUserId, null);
    }

    public BigDecimal calcularBalanceNeto(String externalUserId, String bearerToken) {
        validarExternalUserId(externalUserId);
        verificarSesionUsuario(externalUserId, bearerToken);
        List<Transaccion> transacciones = tGateway.obtenerTransaccionesPorUsuario(externalUserId);
        return calcularBalanceNeto(transacciones);
    }

    public BigDecimal calcularBalanceNeto(List<Transaccion> transacciones) {
        String referenceCurrency = resolveReferenceCurrency(transacciones, null);
        Map<String, BigDecimal> ratesCache = precalcularTasasDeCambio(transacciones, null, referenceCurrency);
        BigDecimal ingresos = calcularIngresosMensuales(transacciones, referenceCurrency, ratesCache);
        BigDecimal gastos = calcularGastosMensuales(transacciones, referenceCurrency, ratesCache);
        return ingresos.subtract(gastos).setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }

    public BigDecimal calcularCompromisoAhorroMensual(List<MetaAhorro> metas) {
        if (metas == null || metas.isEmpty()) {
            return BigDecimal.ZERO.setScale(DECIMAL_SCALE, ROUNDING_MODE);
        }
        String referenceCurrency = resolveReferenceCurrency(null, metas);
        Map<String, BigDecimal> ratesCache = precalcularTasasDeCambio(null, metas, referenceCurrency);
        return calcularCompromisoAhorroMensual(metas, referenceCurrency, ratesCache);
    }

    private BigDecimal calcularCompromisoAhorroMensual(
            List<MetaAhorro> metas,
            String referenceCurrency,
            Map<String, BigDecimal> ratesCache
    ) {
        if (metas == null || metas.isEmpty()) {
            return BigDecimal.ZERO.setScale(DECIMAL_SCALE, ROUNDING_MODE);
        }
        return metas.stream()
                .filter(m -> m != null && m.getMonthlyContribution() != null && m.getMonthlyContribution().compareTo(BigDecimal.ZERO) > 0)
                .map(m -> convertirMontoAMonedaReferencia(
                        m.getMonthlyContribution(),
                        m.getCurrency(),
                        referenceCurrency,
                        ratesCache
                ))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }

    public BigDecimal calcularSaldoInicialAcumulado(List<Transaccion> transacciones) {
        if (transacciones == null || transacciones.isEmpty()) {
            return BigDecimal.ZERO.setScale(DECIMAL_SCALE, ROUNDING_MODE);
        }
        String referenceCurrency = resolveReferenceCurrency(transacciones, null);
        Map<String, BigDecimal> ratesCache = precalcularTasasDeCambio(transacciones, null, referenceCurrency);
        return calcularSaldoInicialAcumulado(transacciones, referenceCurrency, ratesCache);
    }

    private BigDecimal calcularSaldoInicialAcumulado(
            List<Transaccion> transacciones,
            String referenceCurrency,
            Map<String, BigDecimal> ratesCache
    ) {
        if (transacciones == null || transacciones.isEmpty()) {
            return BigDecimal.ZERO.setScale(DECIMAL_SCALE, ROUNDING_MODE);
        }
        return transacciones.stream()
                .filter(t -> t != null && t.getFrequency() == FrecuenciaType.ONE_TIME && t.getAmount() != null)
                .map(t -> {
                    BigDecimal converted = convertirMontoAMonedaReferencia(
                            t.getAmount(),
                            t.getCurrency(),
                            referenceCurrency,
                            ratesCache
                    );
                    return t.getType() == TransaccionType.INCOME ? converted : converted.negate();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }

    // 3. Proyección a n Meses (Projections)
    public BigDecimal proyectarSaldo(String externalUserId, int meses) {
        return proyectarSaldo(externalUserId, meses, null, null);
    }

    public BigDecimal proyectarSaldo(String externalUserId, int meses, String userEmail) {
        return proyectarSaldo(externalUserId, meses, userEmail, null);
    }

    public BigDecimal proyectarSaldo(String externalUserId, int meses, String userEmail, String bearerToken) {
        validarExternalUserId(externalUserId);
        verificarSesionUsuario(externalUserId, bearerToken);
        List<Transaccion> transacciones = tGateway.obtenerTransaccionesPorUsuario(externalUserId);
        String referenceCurrency = resolveReferenceCurrency(transacciones, null);
        Map<String, BigDecimal> ratesCache = precalcularTasasDeCambio(transacciones, null, referenceCurrency);
        BigDecimal saldoInicial = calcularSaldoInicialAcumulado(transacciones, referenceCurrency, ratesCache);
        BigDecimal ingresos = calcularIngresosMensuales(transacciones, referenceCurrency, ratesCache);
        BigDecimal gastos = calcularGastosMensuales(transacciones, referenceCurrency, ratesCache);
        BigDecimal balanceNeto = ingresos.subtract(gastos).setScale(DECIMAL_SCALE, ROUNDING_MODE);
        BigDecimal saldoProyectado = proyectarSaldo(saldoInicial, balanceNeto, meses);

        if (notificationGateway != null
                && userEmail != null
                && !userEmail.isBlank()
                && saldoProyectado != null
                && saldoProyectado.compareTo(BigDecimal.ZERO) < 0) {
            try {
                notificationGateway.notifyProjectionDeficit(
                        userEmail,
                        String.valueOf(meses),
                        saldoProyectado.toPlainString()
                );
            } catch (Exception ignored) {
            }
        }

        return saldoProyectado;
    }

    public BigDecimal proyectarSaldo(BigDecimal saldoInicial, BigDecimal balanceNeto, int meses) {
        BigDecimal saldo = saldoInicial != null ? saldoInicial : BigDecimal.ZERO;
        BigDecimal balance = balanceNeto != null ? balanceNeto : BigDecimal.ZERO;
        BigDecimal mesesBD = BigDecimal.valueOf(meses);
        return saldo.add(balance.multiply(mesesBD)).setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }

    public ResumenFinanciero obtenerResumenFinanciero(String externalUserId) {
        return obtenerResumenFinanciero(externalUserId, null);
    }

    public ResumenFinanciero obtenerResumenFinanciero(String externalUserId, String bearerToken) {
        validarExternalUserId(externalUserId);
        verificarSesionUsuario(externalUserId, bearerToken);
        return proyectarSaldo(externalUserId);
    }

    public ResumenFinanciero proyectarSaldo(String externalUserId) {
        validarExternalUserId(externalUserId);
        List<Transaccion> transaccionesFromGateway = tGateway.obtenerTransaccionesPorUsuario(externalUserId);
        final List<Transaccion> transacciones = transaccionesFromGateway != null ? transaccionesFromGateway : Collections.emptyList();

        List<MetaAhorro> metasFromGateway = mGateway.obtenerMetasPorUsuario(externalUserId);
        final List<MetaAhorro> metas = metasFromGateway != null ? metasFromGateway : Collections.emptyList();

        String moneda = resolveReferenceCurrency(transacciones, metas);
        Map<String, BigDecimal> ratesCache = precalcularTasasDeCambio(transacciones, metas, moneda);

        BigDecimal ingresosMensuales = calcularIngresosMensuales(transacciones, moneda, ratesCache);
        BigDecimal gastosMensuales = calcularGastosMensuales(transacciones, moneda, ratesCache);
        BigDecimal balanceNetoMensual = ingresosMensuales.subtract(gastosMensuales).setScale(DECIMAL_SCALE, ROUNDING_MODE);
        BigDecimal compromisoAhorroMensual = calcularCompromisoAhorroMensual(metas, moneda, ratesCache);
        BigDecimal margenLibre = balanceNetoMensual.subtract(compromisoAhorroMensual).setScale(DECIMAL_SCALE, ROUNDING_MODE);

        BigDecimal saldoInicial = calcularSaldoInicialAcumulado(transacciones, moneda, ratesCache);
        BigDecimal proyeccion1Ano = proyectarSaldo(saldoInicial, balanceNetoMensual, 12);
        Map<String, BigDecimal> porCategoria = calcularDesglosePorCategoria(
                externalUserId,
                transacciones,
                moneda,
                ratesCache
        );

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
        resumen.setPorCategoria(porCategoria);

        return resumen;
    }

    private Map<String, BigDecimal> calcularDesglosePorCategoria(
            String externalUserId,
            List<Transaccion> transacciones,
            String referenceCurrency,
            Map<String, BigDecimal> ratesCache
    ) {
        Map<String, BigDecimal> porCategoria = new LinkedHashMap<>();
        if (transacciones == null || transacciones.isEmpty()) {
            return porCategoria;
        }

        Map<String, String> categoryNamesById = new HashMap<>();
        if (categoriaGateway != null) {
            List<Categoria> categorias = categoriaGateway.obtenerCategoriasPorUsuario(externalUserId);
            if (categorias != null) {
                for (Categoria cat : categorias) {
                    if (cat != null && cat.getId() != null && cat.getName() != null && !cat.getName().trim().isEmpty()) {
                        categoryNamesById.put(cat.getId().trim(), cat.getName().trim());
                    }
                }
            }
        }

        for (Transaccion t : transacciones) {
            if (t == null || t.getAmount() == null) {
                continue;
            }
            String categoryName = CATEGORIA_SIN_ASIGNAR;
            if (t.getCategoryId() != null && !t.getCategoryId().trim().isEmpty()) {
                String resolvedName = categoryNamesById.get(t.getCategoryId().trim());
                if (resolvedName != null) {
                    categoryName = resolvedName;
                }
            }
            BigDecimal convertedAmount = convertirMontoAMonedaReferencia(
                    t.getAmount().setScale(DECIMAL_SCALE, ROUNDING_MODE),
                    t.getCurrency(),
                    referenceCurrency,
                    ratesCache
            );
            porCategoria.merge(
                    categoryName,
                    convertedAmount,
                    (current, added) -> current.add(added).setScale(DECIMAL_SCALE, ROUNDING_MODE)
            );
        }

        return porCategoria;
    }

    private Map<String, BigDecimal> precalcularTasasDeCambio(
            List<Transaccion> transacciones,
            List<MetaAhorro> metas,
            String referenceCurrency
    ) {
        Map<String, BigDecimal> ratesCache = new HashMap<>();
        if (exchangeRateGateway == null || referenceCurrency == null || referenceCurrency.trim().isEmpty()) {
            return ratesCache;
        }
        String normalizedRef = referenceCurrency.trim().toUpperCase();
        Set<String> distinctCurrencies = new LinkedHashSet<>();

        if (transacciones != null) {
            for (Transaccion t : transacciones) {
                if (t != null && t.getCurrency() != null && !t.getCurrency().trim().isEmpty()) {
                    String normalizedSource = t.getCurrency().trim().toUpperCase();
                    if (!normalizedSource.equals(normalizedRef)) {
                        distinctCurrencies.add(normalizedSource);
                    }
                }
            }
        }
        if (metas != null) {
            for (MetaAhorro m : metas) {
                if (m != null && m.getCurrency() != null && !m.getCurrency().trim().isEmpty()) {
                    String normalizedSource = m.getCurrency().trim().toUpperCase();
                    if (!normalizedSource.equals(normalizedRef)) {
                        distinctCurrencies.add(normalizedSource);
                    }
                }
            }
        }

        for (String sourceCurrency : distinctCurrencies) {
            BigDecimal rate = exchangeRateGateway.getExchangeRate(sourceCurrency, normalizedRef);
            if (rate != null && rate.compareTo(BigDecimal.ZERO) > 0) {
                ratesCache.put(sourceCurrency, rate);
            }
        }
        return ratesCache;
    }

    private void verificarSesionUsuario(String externalUserId, String bearerToken) {
        if (accountVerificationGateway != null) {
            VerifiedUserSession session = accountVerificationGateway.verifyLoggedUser(externalUserId, bearerToken);
            if (session == null || session.id() == null || session.id().isBlank()) {
                throw new IllegalStateException("La sesión del usuario no pudo ser verificada");
            }
        }
    }

    private String resolveReferenceCurrency(List<Transaccion> transacciones, List<MetaAhorro> metas) {
        if (transacciones != null) {
            for (Transaccion t : transacciones) {
                if (t != null && t.getCurrency() != null && !t.getCurrency().trim().isEmpty()) {
                    return t.getCurrency().trim().toUpperCase();
                }
            }
        }
        if (metas != null) {
            for (MetaAhorro m : metas) {
                if (m != null && m.getCurrency() != null && !m.getCurrency().trim().isEmpty()) {
                    return m.getCurrency().trim().toUpperCase();
                }
            }
        }
        return "USD";
    }

    private BigDecimal convertirMontoAMonedaReferencia(
            BigDecimal monto,
            String sourceCurrency,
            String referenceCurrency,
            Map<String, BigDecimal> ratesCache
    ) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(DECIMAL_SCALE, ROUNDING_MODE);
        }
        if (sourceCurrency == null || sourceCurrency.trim().isEmpty() || referenceCurrency == null || referenceCurrency.trim().isEmpty()) {
            return monto;
        }
        String normalizedSource = sourceCurrency.trim().toUpperCase();
        String normalizedRef = referenceCurrency.trim().toUpperCase();
        if (normalizedSource.equals(normalizedRef) || exchangeRateGateway == null) {
            return monto;
        }
        BigDecimal rate = ratesCache != null ? ratesCache.get(normalizedSource) : null;
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            return monto;
        }
        return monto.multiply(rate).setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }

    private void validarExternalUserId(String externalUserId) {
        if (externalUserId == null || externalUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID externo del usuario no puede ser nulo o vacío.");
        }
    }
}
