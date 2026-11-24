package com.finance.transaction.domain.usecase;

import com.finance.transaction.domain.model.ConvertedBalance;
import com.finance.transaction.domain.model.CurrencyMetadata;
import com.finance.transaction.domain.model.Transaccion;
import com.finance.transaction.domain.model.VerifiedUserSession;
import com.finance.transaction.domain.model.enumns.FrecuenciaType;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import com.finance.transaction.domain.model.gateway.AccountVerificationGateway;
import com.finance.transaction.domain.model.gateway.CurrencyMetadataGateway;
import com.finance.transaction.domain.model.gateway.ExchangeRateGateway;
import com.finance.transaction.domain.model.gateway.TransaccionGateway;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DivisasUseCase {

    private final TransaccionGateway transaccionGateway;
    private final ExchangeRateGateway exchangeRateGateway;
    private final CurrencyMetadataGateway currencyMetadataGateway;
    private final AccountVerificationGateway accountVerificationGateway;

    public DivisasUseCase(
            TransaccionGateway transaccionGateway,
            ExchangeRateGateway exchangeRateGateway,
            CurrencyMetadataGateway currencyMetadataGateway
    ) {
        this(transaccionGateway, exchangeRateGateway, currencyMetadataGateway, null);
    }

    public DivisasUseCase(
            TransaccionGateway transaccionGateway,
            ExchangeRateGateway exchangeRateGateway,
            CurrencyMetadataGateway currencyMetadataGateway,
            AccountVerificationGateway accountVerificationGateway
    ) {
        this.transaccionGateway = transaccionGateway;
        this.exchangeRateGateway = exchangeRateGateway;
        this.currencyMetadataGateway = currencyMetadataGateway;
        this.accountVerificationGateway = accountVerificationGateway;
    }

    public ConvertedBalance convertirBalanceMensual(String externalUserId, String targetCurrency) {
        return convertirBalanceMensual(externalUserId, targetCurrency, null);
    }

    public ConvertedBalance convertirBalanceMensual(String externalUserId, String targetCurrency, String bearerToken) {
        if (externalUserId == null || externalUserId.isBlank()) {
            throw new IllegalArgumentException("El ID externo del usuario no puede ser nulo o vacío.");
        }
        String userBaseCurrency = validateAccountSession(externalUserId, bearerToken);
        if (targetCurrency == null || !targetCurrency.trim().toUpperCase().matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("La moneda destino debe ser un código ISO de 3 letras");
        }

        String normalizedTarget = targetCurrency.trim().toUpperCase();
        CurrencyMetadata metadata = null;
        try {
            metadata = currencyMetadataGateway.getCurrencyMetadata(normalizedTarget);
        } catch (RuntimeException ignored) {
            metadata = null;
        }
        String targetCurrencyName = (metadata != null && metadata.name() != null && !metadata.name().isBlank())
                ? metadata.name()
                : normalizedTarget;
        String targetCurrencySymbol = (metadata != null && metadata.symbol() != null && !metadata.symbol().isBlank())
                ? metadata.symbol()
                : "$";
        List<String> targetCurrencyCountries = (metadata != null && metadata.countries() != null)
                ? metadata.countries()
                : Collections.emptyList();

        List<Transaccion> transacciones = transaccionGateway.obtenerTransaccionesPorUsuario(externalUserId);
        if (transacciones == null || transacciones.isEmpty()) {
            return new ConvertedBalance(
                    externalUserId,
                    normalizedTarget,
                    targetCurrencyName,
                    targetCurrencySymbol,
                    targetCurrencyCountries,
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    0
            );
        }

        Map<String, BigDecimal> ratesCache = precalcularTasasDeCambio(transacciones, normalizedTarget, userBaseCurrency);
        BigDecimal totalConverted = BigDecimal.ZERO;
        int processedCount = 0;

        for (Transaccion tx : transacciones) {
            if (tx == null || tx.getAmount() == null || tx.getType() == null) {
                continue;
            }

            FrecuenciaType effectiveFrequency = tx.getFrequency() != null ? tx.getFrequency() : FrecuenciaType.ONE_TIME;
            BigDecimal monthlyAmount = FrecuenciaType.normalizarMontoMensual(tx.getAmount(), effectiveFrequency);
            String sourceCurrency = normalizeSourceCurrency(tx.getCurrency(), userBaseCurrency);

            BigDecimal convertedAmount;
            if (sourceCurrency.equals(normalizedTarget)) {
                convertedAmount = monthlyAmount;
            } else {
                BigDecimal rate = ratesCache.get(sourceCurrency);
                if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Tasa de cambio no disponible para la moneda solicitada");
                }
                convertedAmount = monthlyAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            }

            if (tx.getType() == TransaccionType.INCOME) {
                totalConverted = totalConverted.add(convertedAmount);
            } else if (tx.getType() == TransaccionType.EXPENSE) {
                totalConverted = totalConverted.subtract(convertedAmount);
            }
            processedCount++;
        }

        return new ConvertedBalance(
                externalUserId,
                normalizedTarget,
                targetCurrencyName,
                targetCurrencySymbol,
                targetCurrencyCountries,
                totalConverted.setScale(2, RoundingMode.HALF_UP),
                processedCount
        );
    }

    private Map<String, BigDecimal> precalcularTasasDeCambio(
            List<Transaccion> transacciones,
            String normalizedTarget,
            String userBaseCurrency
    ) {
        Set<String> distinctSourceCurrencies = new LinkedHashSet<>();
        for (Transaccion tx : transacciones) {
            if (tx == null || tx.getAmount() == null || tx.getType() == null) {
                continue;
            }
            String sourceCurrency = normalizeSourceCurrency(tx.getCurrency(), userBaseCurrency);
            if (!sourceCurrency.equals(normalizedTarget)) {
                distinctSourceCurrencies.add(sourceCurrency);
            }
        }

        Map<String, BigDecimal> ratesCache = new HashMap<>();
        for (String sourceCurrency : distinctSourceCurrencies) {
            BigDecimal rate = exchangeRateGateway.getExchangeRate(sourceCurrency, normalizedTarget);
            if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Tasa de cambio no disponible para la moneda solicitada");
            }
            ratesCache.put(sourceCurrency, rate);
        }
        return ratesCache;
    }

    private String normalizeSourceCurrency(String rawCurrency, String fallbackBaseCurrency) {
        if (rawCurrency == null || rawCurrency.isBlank()) {
            return (fallbackBaseCurrency == null || fallbackBaseCurrency.isBlank())
                    ? "USD"
                    : fallbackBaseCurrency.trim().toUpperCase();
        }
        return rawCurrency.trim().toUpperCase();
    }

    public CurrencyMetadata consultarMetadatosMoneda(String currencyCode) {
        if (currencyCode == null || !currencyCode.trim().toUpperCase().matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("El código de moneda debe ser un código ISO de 3 letras");
        }
        String normalizedCode = currencyCode.trim().toUpperCase();
        String officialName = exchangeRateGateway.getOfficialCurrencyName(normalizedCode)
                .orElseThrow(() -> new IllegalArgumentException("Moneda no soportada por ExchangeRate-API: " + normalizedCode));

        return new CurrencyMetadata(
                normalizedCode,
                officialName,
                resolveStandardSymbol(normalizedCode),
                resolveStandardCountries(normalizedCode)
        );
    }

    private String resolveStandardSymbol(String normalizedCode) {
        return switch (normalizedCode) {
            case "COP", "USD", "MXN", "ARS", "CLP", "CAD" -> "$";
            case "EUR" -> "€";
            case "GBP" -> "£";
            case "JPY" -> "¥";
            case "BRL" -> "R$";
            case "PEN" -> "S/";
            case "CHF" -> "Fr";
            default -> normalizedCode;
        };
    }

    private List<String> resolveStandardCountries(String normalizedCode) {
        return switch (normalizedCode) {
            case "COP" -> List.of("Colombia");
            case "USD" -> List.of("United States");
            case "EUR" -> List.of("European Union");
            case "MXN" -> List.of("Mexico");
            case "GBP" -> List.of("United Kingdom");
            case "JPY" -> List.of("Japan");
            case "BRL" -> List.of("Brazil");
            case "ARS" -> List.of("Argentina");
            case "CLP" -> List.of("Chile");
            case "PEN" -> List.of("Peru");
            case "CAD" -> List.of("Canada");
            case "CHF" -> List.of("Switzerland");
            default -> List.of("International");
        };
    }

    private String validateAccountSession(String externalUserId, String bearerToken) {
        if (accountVerificationGateway != null) {
            VerifiedUserSession session = accountVerificationGateway.verifyLoggedUser(externalUserId, bearerToken);
            if (session == null || session.id() == null || session.id().isBlank()) {
                throw new IllegalStateException("La sesión del usuario no pudo ser verificada");
            }
            return session.resolvedBaseCurrency();
        }
        return "USD";
    }
}
