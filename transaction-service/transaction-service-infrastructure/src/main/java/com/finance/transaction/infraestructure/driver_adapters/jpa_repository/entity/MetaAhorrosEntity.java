package com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "savings_goals")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetaAhorrosEntity {

    @Id
    private String id;
    private String externalUserId;
    private String title;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private BigDecimal monthlyContribution;
    private String currency;
    private LocalDate targetDate;
    private LocalDateTime createdAt;

}