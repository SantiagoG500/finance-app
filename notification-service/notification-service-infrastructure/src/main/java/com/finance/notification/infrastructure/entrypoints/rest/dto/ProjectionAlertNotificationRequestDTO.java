package com.finance.notification.infrastructure.entrypoints.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProjectionAlertNotificationRequestDTO(
        @NotBlank(message = "El correo electrónico es requerido")
        @Email(message = "El formato del correo electrónico no es válido")
        String email,

        @NotBlank(message = "El horizonte de meses es requerido")
        String monthsHorizon,

        @NotBlank(message = "El saldo proyectado es requerido")
        String projectedBalance
) {
}
