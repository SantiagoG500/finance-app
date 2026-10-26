package com.finance.transaction.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class MetaAhorro {

    private String id;
    private String externalUserId;
    private String title;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private BigDecimal monthlyContribution;
    private String currency;
    private LocalDate targetDate;
    private LocalDateTime createdAt;

    public Double calcularPorcentajeCompletado() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        if (currentAmount == null || currentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }

        BigDecimal ratio = currentAmount.divide(targetAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        return ratio.doubleValue();
    }

    public Integer calcularMesesEstimados() {
        if (targetAmount == null || monthlyContribution == null || monthlyContribution.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        BigDecimal montoActual = (currentAmount != null) ? currentAmount : BigDecimal.ZERO;
        BigDecimal restante = targetAmount.subtract(montoActual);

        if (restante.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        BigDecimal meses = restante.divide(monthlyContribution, 2, RoundingMode.CEILING);
        return (int) Math.ceil(meses.doubleValue());
    }

}
