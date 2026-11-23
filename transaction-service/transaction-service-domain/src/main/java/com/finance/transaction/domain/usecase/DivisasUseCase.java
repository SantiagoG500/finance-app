package com.finance.transaction.domain.usecase;

import com.finance.transaction.domain.model.ConvertedBalance;
import com.finance.transaction.domain.model.CurrencyMetadata;
import com.finance.transaction.domain.model.gateway.CurrencyMetadataGateway;
import com.finance.transaction.domain.model.gateway.ExchangeRateGateway;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class DivisasUseCase {

    private static final Pattern ISO_CURRENCY_PATTERN = Pattern.compile("^[A-Za-z]{3}$");

    private final ProyeccionesUseCase proyeccionesUseCase;
    private final ExchangeRateGateway exchangeRateGateway;
    private final CurrencyMetadataGateway currencyMetadataGateway;

    public ConvertedBalance convertirBalanceMensual(
            String externalUserId,
            String baseCurrency,
            String targetCurrency
    ) {
        if (externalUserId == null || externalUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID externo del usuario no puede ser nulo o vacío.");
        }

        String normalizedBase = normalizeAndValidateCurrencyCode(baseCurrency, "La moneda origen debe ser un código ISO de 3 letras");
        String normalizedTarget = normalizeAndValidateCurrencyCode(targetCurrency, "La moneda destino debe ser un código ISO de 3 letras");

        BigDecimal balanceNeto = proyeccionesUseCase.calcularBalanceNeto(externalUserId);
        BigDecimal rate;
        if (normalizedBase.equals(normalizedTarget)) {
            rate = BigDecimal.ONE;
        } else {
            rate = exchangeRateGateway.getExchangeRate(normalizedBase, normalizedTarget);
            if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Tasa de cambio no disponible para la moneda solicitada");
            }
        }

        BigDecimal converted = balanceNeto.multiply(rate).setScale(2, RoundingMode.HALF_UP);

        return new ConvertedBalance(
                externalUserId,
                normalizedBase,
                normalizedTarget,
                rate,
                balanceNeto,
                converted
        );
    }

    public CurrencyMetadata consultarMetadatosMoneda(String currencyCode) {
        String normalizedCode = normalizeAndValidateCurrencyCode(currencyCode, "El código de moneda debe ser un código ISO de 3 letras");
        return currencyMetadataGateway.getCurrencyMetadata(normalizedCode);
    }

    private String normalizeAndValidateCurrencyCode(String currencyCode, String errorMessage) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        String trimmed = currencyCode.trim();
        if (!ISO_CURRENCY_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return trimmed.toUpperCase();
    }
}
