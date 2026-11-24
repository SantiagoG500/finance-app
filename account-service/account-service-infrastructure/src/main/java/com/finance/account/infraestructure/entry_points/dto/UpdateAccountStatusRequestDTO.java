package com.finance.account.infraestructure.entry_points.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
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

    @NotNull(message = "El estado activo/inactivo es obligatorio")
    @JsonAlias({"isActive", "active"})
    private Boolean active;
}
