package com.finance.notification.infrastructure.entrypoints.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AccountStatusNotificationRequestDTO(
        @NotBlank(message = "El correo electrónico es requerido")
        @Email(message = "El formato del correo electrónico no es válido")
        String email,

        @NotBlank(message = "El estado de la cuenta es requerido")
        String status
) {
}
