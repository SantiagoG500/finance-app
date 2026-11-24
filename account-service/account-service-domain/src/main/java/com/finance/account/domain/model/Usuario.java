package com.finance.account.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {

    private UUID id;
    private String email;
    private String passwordHash;
    private String fullName;
    private String avatarUrl;
    private String baseCurrency;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public Usuario(UUID id, String email, String passwordHash, String fullName, String avatarUrl, String baseCurrency, Boolean isActive) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.baseCurrency = baseCurrency;
        this.isActive = isActive;
    }
}
