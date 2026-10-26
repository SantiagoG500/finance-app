package com.finance.transaction.domain.model;

import com.finance.transaction.domain.model.enumns.FrecuenciaType;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Transaccion {

    private String id;
    private String externalUserId;
    private String categoryId;
    private BigDecimal amount;
    private String currency;
    private TransaccionType type;
    private FrecuenciaType frequency;
    private String Description;
    private LocalDate transactionDate;
    private LocalDateTime createdAt;

}
