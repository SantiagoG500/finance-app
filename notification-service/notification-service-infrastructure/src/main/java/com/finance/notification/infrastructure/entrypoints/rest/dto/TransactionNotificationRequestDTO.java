package com.finance.notification.infrastructure.entrypoints.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TransactionNotificationRequestDTO(
        @NotBlank(message = "El correo electrónico es requerido")
        @Email(message = "El formato del correo electrónico no es válido")
        String email,

        @NotBlank(message = "El tipo de transacción es requerido")
        String transactionType,

        @NotBlank(message = "El monto es requerido")
        String amount,

        @NotBlank(message = "La descripción es requerida")
        String description
) {
}
