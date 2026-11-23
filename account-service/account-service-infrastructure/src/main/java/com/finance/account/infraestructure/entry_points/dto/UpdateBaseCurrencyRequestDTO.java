package com.finance.account.infraestructure.entry_points.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBaseCurrencyRequestDTO {

    @NotBlank(message = "La moneda base es obligatoria")
    private String baseCurrency;

}
