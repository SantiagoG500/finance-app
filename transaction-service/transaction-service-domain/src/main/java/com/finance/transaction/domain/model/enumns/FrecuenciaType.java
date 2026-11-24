package com.finance.transaction.domain.model.enumns;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum FrecuenciaType {
    ONE_TIME,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY;

    public static final Integer FACTOR_DAILY = Integer.valueOf(30);
    public static final Integer FACTOR_WEEKLY = Integer.valueOf(4);
    public static final Integer FACTOR_MONTHLY = Integer.valueOf(1);
    public static final Integer FACTOR_ONE_TIME = Integer.valueOf(1);
    public static final Integer DIVISOR_YEARLY = Integer.valueOf(12);
    private static final Integer ESCALA_MONETARIA = Integer.valueOf(2);

    public static BigDecimal normalizarMontoMensual(BigDecimal amount, FrecuenciaType frequency) {
        if (amount == null || frequency == null) {
            return BigDecimal.ZERO.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
        }
        return switch (frequency) {
            case DAILY -> amount.multiply(BigDecimal.valueOf(FACTOR_DAILY))
                    .setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
            case WEEKLY -> amount.multiply(BigDecimal.valueOf(FACTOR_WEEKLY))
                    .setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
            case MONTHLY -> amount.multiply(BigDecimal.valueOf(FACTOR_MONTHLY))
                    .setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
            case ONE_TIME -> amount.multiply(BigDecimal.valueOf(FACTOR_ONE_TIME))
                    .setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
            case YEARLY -> amount.divide(BigDecimal.valueOf(DIVISOR_YEARLY), ESCALA_MONETARIA, RoundingMode.HALF_UP);
        };
    }
}
