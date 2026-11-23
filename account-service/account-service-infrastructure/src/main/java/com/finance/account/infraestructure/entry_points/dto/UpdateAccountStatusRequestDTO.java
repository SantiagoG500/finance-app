package com.finance.account.infraestructure.entry_points.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAccountStatusRequestDTO {

    @NotNull(message = "El estado active es obligatorio")
    private Boolean active;

}
