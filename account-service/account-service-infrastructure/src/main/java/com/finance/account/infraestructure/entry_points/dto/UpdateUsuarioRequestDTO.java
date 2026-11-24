package com.finance.account.infraestructure.entry_points.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUsuarioRequestDTO {

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String fullName;

    @NotBlank(message = "La URL del avatar es obligatoria")
    private String avatarUrl;

    @NotBlank(message = "La moneda base es obligatoria")
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "El código de moneda debe ser un código ISO-4217 de 3 letras (ej: COP, USD, EUR)")
    private String baseCurrency;

    @NotNull(message = "El estado activo/inactivo es obligatorio")
    private Boolean isActive;
}
