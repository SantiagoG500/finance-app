package com.finance.transaction.infraestructure.entry_points.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MetaAhorroResponseDTO {

    private String id;
    private String title;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private BigDecimal monthlyContribution;
    private String currency;
    private LocalDate targetDate;
    private LocalDateTime createdAt;

}