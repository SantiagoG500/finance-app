package com.finance.transaction.infraestructure.entry_points.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.finance.transaction.domain.model.enumns.FrecuenciaType;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateTransaccionRequestDTO {

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency must be a valid 3-letter ISO code")
    private String currency;

    @NotNull(message = "Transaction type is required")
    private TransaccionType type;

    @NotNull(message = "Frequency is required")
    private FrecuenciaType frequency;

    private String description;

    @NotNull(message = "Transaction date is required")
    @JsonAlias("date")
    private LocalDate transactionDate;
}
