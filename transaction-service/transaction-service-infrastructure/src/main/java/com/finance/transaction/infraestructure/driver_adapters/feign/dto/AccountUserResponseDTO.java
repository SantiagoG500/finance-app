package com.finance.transaction.infraestructure.driver_adapters.feign.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountUserResponseDTO {

    private String id;
    private String fullName;
    private String email;

    @JsonAlias({"active", "isActive"})
    private Boolean isActive;

    private String baseCurrency;

    public AccountUserResponseDTO(String id, String fullName, String email, Boolean isActive) {
        this(id, fullName, email, isActive, "USD");
    }

}
