package com.finance.transaction.infraestructure.entry_points.dto;

import com.finance.transaction.domain.model.enumns.FrecuenciaType;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

// TODO: translate the messages to Spanish

@Getter
@AllArgsConstructor
public class TransaccionRequestDTO {

    @NotBlank(message = "Transacción ID is required")
    private String id;

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotNull(message = "Transaction type is required")
    private TransaccionType type;

    @NotNull(message = "Frequency is required")
    private FrecuenciaType frequency;

    private String description;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

}
