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
    private UUID externalId;
    private String email;
    private String displayName;
    private String baseCurrency;

}
