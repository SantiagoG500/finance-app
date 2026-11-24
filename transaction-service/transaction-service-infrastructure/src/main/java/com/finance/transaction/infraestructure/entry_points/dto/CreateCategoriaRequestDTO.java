package com.finance.transaction.infraestructure.entry_points.dto;

import com.finance.transaction.domain.model.enumns.TransaccionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateCategoriaRequestDTO {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String name;

    @NotNull(message = "El tipo de la categoría es obligatorio")
    private TransaccionType type;

}
