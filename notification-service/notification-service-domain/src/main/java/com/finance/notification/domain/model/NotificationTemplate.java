package com.finance.notification.domain.model;

/**
 * Catálogo centralizado de plantillas de correo electrónico para eventos de autenticación.
 * Los textos de asunto y cuerpo pueden editarse manualmente en este archivo.
 * Soporta marcadores de formato estándar de Java (String.format), por ejemplo: %s.
 */
public enum NotificationTemplate {

    AUTH_WELCOME_VERIFICATION(
            "Bienvenido a Finance App - Verifica tu cuenta",
            """
            Hola %s,

            ¡Te damos la bienvenida a Finance App!
            Tu registro e inicio en el proceso de autenticación se ha realizado exitosamente.

            Si no reconoces esta actividad, por favor ignora este mensaje o contacta a soporte.

            Atentamente,
            El equipo de Finance App
            """
    ),

    AUTH_LOGIN_ALERT(
            "Finance App - Nuevo inicio de sesión detectado",
            """
            Hola %s,

            Se ha detectado un nuevo evento de autenticación asociado a tu cuenta (%s).
            Si fuiste tú, no necesitas realizar ninguna acción adicional.

            Atentamente,
            El equipo de Seguridad de Finance App
            """
    );

    private final String subjectTemplate;
    private final String bodyTemplate;

    NotificationTemplate(String subjectTemplate, String bodyTemplate) {
        this.subjectTemplate = subjectTemplate;
        this.bodyTemplate = bodyTemplate;
    }

    public String getSubjectTemplate() {
        return subjectTemplate;
    }

    public String getBodyTemplate() {
        return bodyTemplate;
    }

    public String formatSubject(Object... args) {
        return (args == null || args.length == 0)
                ? subjectTemplate
                : String.format(subjectTemplate, args);
    }

    public String formatBody(Object... args) {
        return (args == null || args.length == 0)
                ? bodyTemplate
                : String.format(bodyTemplate, args);
    }

    public Notification buildNotification(String recipient, Object... bodyArgs) {
        return new Notification(
                recipient,
                this.subjectTemplate,
                formatBody(bodyArgs)
        );
    }
}
