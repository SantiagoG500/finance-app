package com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity;

import com.finance.transaction.domain.model.enumns.FrecuenciaType;
import com.finance.transaction.domain.model.enumns.TransaccionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TransaccionEntity {
    @Id
    private String id;
    private String externalUserId;
    private String categoryId;
    private BigDecimal amount;
    private String currency;

    @Enumerated(EnumType.STRING)
    private TransaccionType type;

    @Enumerated(EnumType.STRING)
    private FrecuenciaType frequency;

    private String description;
    private LocalDate transactionDate;

//    @CreatedDate
// @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}