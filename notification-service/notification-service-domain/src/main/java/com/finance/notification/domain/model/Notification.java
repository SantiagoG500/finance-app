package com.finance.notification.domain.model;

import java.util.regex.Pattern;

public record Notification(String recipient, String subject, String body) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public Notification {
        if (recipient == null || recipient.isBlank() || !EMAIL_PATTERN.matcher(recipient.trim()).matches()) {
            throw new IllegalArgumentException("El formato del correo electrónico del destinatario no es válido: " + recipient);
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("El asunto de la notificación no puede estar vacío.");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("El cuerpo de la notificación no puede estar vacío.");
        }
        recipient = recipient.trim();
    }
}
