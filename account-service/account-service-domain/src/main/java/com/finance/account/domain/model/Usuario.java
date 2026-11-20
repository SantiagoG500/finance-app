package com.finance.account.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Usuario {

    private UUID id;
    private String email;
    private String passwordHash;
    private String fullName;
    private String avatarUrl;
    private String baseCurrency;
    private Boolean isActive;

}
