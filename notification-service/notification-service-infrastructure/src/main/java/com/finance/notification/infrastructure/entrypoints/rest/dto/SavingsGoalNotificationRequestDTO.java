package com.finance.notification.infrastructure.entrypoints.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SavingsGoalNotificationRequestDTO(
        @NotBlank(message = "El correo electrónico es requerido")
        @Email(message = "El formato del correo electrónico no es válido")
        String email,

        @NotBlank(message = "El título de la meta es requerido")
        String goalTitle,

        @NotBlank(message = "El resumen de progreso es requerido")
        String progressSummary,

        @NotBlank(message = "El porcentaje completado es requerido")
        String percentage
) {
}
