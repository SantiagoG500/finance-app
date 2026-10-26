package com.finance.transaction.infraestructure.entry_points.dto;

import com.finance.transaction.domain.model.enumns.FrecuenciaType;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransaccionResponseDTO {

    private String id;
    private String categoryId;
    private BigDecimal amount;
    private String currency;
    private TransaccionType type;
    private FrecuenciaType frequency;
    private String description;
    private LocalDate transactionDate;
    private LocalDateTime createdAt;

}
