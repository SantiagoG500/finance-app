package com.finance.notification.domain.model;

/**
 * Catálogo centralizado de plantillas HTML de correo electrónico para eventos de autenticación.
 * Los textos de asunto y cuerpo pueden editarse manualmente en este archivo.
 * Soporta marcadores de formato estándar de Java (String.format), por ejemplo: %s.
 */
public enum NotificationTemplate {

    AUTH_WELCOME_VERIFICATION(
            "Bienvenido a Finance App - Verifica tu cuenta",
            """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; background-color: #0B0D0C; font-family: 'Segoe UI', Arial, sans-serif; color: #F3F4F6;">
                <table role="presentation" width="600" cellspacing="0" cellpadding="0" border="0" align="center" style="max-width: 600px; margin: 32px auto; background-color: #141816; border: 1px solid #D4AF37; border-radius: 12px; overflow: hidden;">
                    <!-- Cabecera -->
                    <tr>
                        <td style="background: linear-gradient(135deg, #0B0D0C 0%%, #1A241E 100%%); padding: 28px 32px; text-align: center; border-bottom: 2px solid #D4AF37;">
                            <div style="display: inline-block; padding: 8px 16px; border: 1px solid #D4AF37; border-radius: 6px; margin-bottom: 12px;">
                                <span style="font-size: 20px; font-weight: 800; letter-spacing: 3px; color: #D4AF37;">FI</span>
                            </div>
                            <h1 style="margin: 0; font-size: 22px; font-weight: 700; color: #F3D06B; letter-spacing: 0.5px;">
                                FINANCE APP
                            </h1>
                            <p style="margin: 6px 0 0; font-size: 12px; text-transform: uppercase; letter-spacing: 2px; color: #10B981;">
                                Gestión Financiera Inteligente
                            </p>
                        </td>
                    </tr>

                    <!-- Cuerpo principal -->
                    <tr>
                        <td style="padding: 36px 32px;">
                            <div style="display: inline-block; background-color: rgba(16, 185, 129, 0.15); border: 1px solid #10B981; color: #10B981; font-size: 12px; font-weight: 600; padding: 4px 12px; border-radius: 20px; margin-bottom: 20px;">
                                &#10003; Registro Completado
                            </div>

                            <h2 style="margin: 0 0 16px; font-size: 20px; color: #FFFFFF;">
                                Hola <span style="color: #F3D06B;">%s</span>,
                            </h2>

                            <p style="margin: 0 0 16px; font-size: 15px; line-height: 1.6; color: #D1D5DB;">
                                ¡Te damos la bienvenida a <strong style="color: #F3D06B;">Finance App</strong>! Tu registro e inicio en el proceso de autenticación se ha realizado exitosamente.
                            </p>

                            <table role="presentation" width="536" cellspacing="0" cellpadding="0" border="0" style="margin: 24px 0; background-color: #0B0D0C; border-left: 4px solid #10B981; border-radius: 6px;">
                                <tr>
                                    <td style="padding: 16px 20px; font-size: 14px; line-height: 1.5; color: #E5E7EB;">
                                        Tu cuenta ya está vinculada y lista para gestionar tus finanzas personales de forma segura.
                                    </td>
                                </tr>
                            </table>

                            <p style="margin: 24px 0 0; font-size: 13px; line-height: 1.5; color: #9CA3AF;">
                                Si no reconoces esta actividad, por favor ignora este mensaje o contacta de inmediato con nuestro equipo de soporte.
                            </p>
                        </td>
                    </tr>

                    <!-- Pie de página -->
                    <tr>
                        <td style="background-color: #0B0D0C; padding: 20px 32px; text-align: center; border-top: 1px solid #262B28;">
                            <p style="margin: 0; font-size: 13px; color: #D4AF37; font-weight: 600;">
                                Atentamente, el equipo de Finance App
                            </p>
                            <p style="margin: 6px 0 0; font-size: 11px; color: #6B7280;">
                                Este es un correo automático de seguridad. Por favor no respondas a esta dirección.
                            </p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """
    ),

    AUTH_LOGIN_ALERT(
            "Finance App - Nuevo inicio de sesión detectado",
            """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; background-color: #0B0D0C; font-family: 'Segoe UI', Arial, sans-serif; color: #F3F4F6;">
                <table role="presentation" width="600" cellspacing="0" cellpadding="0" border="0" align="center" style="max-width: 600px; margin: 32px auto; background-color: #141816; border: 1px solid #D4AF37; border-radius: 12px; overflow: hidden;">
                    <!-- Cabecera -->
                    <tr>
                        <td style="background: linear-gradient(135deg, #0B0D0C 0%%, #1A241E 100%%); padding: 28px 32px; text-align: center; border-bottom: 2px solid #D4AF37;">
                            <div style="display: inline-block; padding: 8px 16px; border: 1px solid #D4AF37; border-radius: 6px; margin-bottom: 12px;">
                                <span style="font-size: 20px; font-weight: 800; letter-spacing: 3px; color: #D4AF37;">FI</span>
                            </div>
                            <h1 style="margin: 0; font-size: 22px; font-weight: 700; color: #F3D06B; letter-spacing: 0.5px;">
                                FINANCE APP
                            </h1>
                            <p style="margin: 6px 0 0; font-size: 12px; text-transform: uppercase; letter-spacing: 2px; color: #D4AF37;">
                                Alerta de Seguridad
                            </p>
                        </td>
                    </tr>

                    <!-- Cuerpo principal -->
                    <tr>
                        <td style="padding: 36px 32px;">
                            <div style="display: inline-block; background-color: rgba(212, 175, 55, 0.15); border: 1px solid #D4AF37; color: #F3D06B; font-size: 12px; font-weight: 600; padding: 4px 12px; border-radius: 20px; margin-bottom: 20px;">
                                &#9888; Actividad de Autenticación
                            </div>

                            <h2 style="margin: 0 0 16px; font-size: 20px; color: #FFFFFF;">
                                Hola <span style="color: #F3D06B;">%s</span>,
                            </h2>

                            <p style="margin: 0 0 16px; font-size: 15px; line-height: 1.6; color: #D1D5DB;">
                                Se ha detectado un nuevo evento de inicio de sesión asociado a tu cuenta:
                            </p>

                            <table role="presentation" width="536" cellspacing="0" cellpadding="0" border="0" style="margin: 20px 0; background-color: #0B0D0C; border: 1px solid #262B28; border-left: 4px solid #D4AF37; border-radius: 6px;">
                                <tr>
                                    <td style="padding: 16px 20px; font-size: 14px; color: #E5E7EB;">
                                        <span style="color: #9CA3AF;">Cuenta verificada:</span><br>
                                        <strong style="color: #10B981; font-size: 15px;">%s</strong>
                                    </td>
                                </tr>
                            </table>

                            <p style="margin: 20px 0 0; font-size: 14px; line-height: 1.6; color: #D1D5DB;">
                                Si fuiste tú, no necesitas realizar ninguna acción adicional. Si no reconoces este acceso, asegura tu cuenta de Google de inmediato.
                            </p>
                        </td>
                    </tr>

                    <!-- Pie de página -->
                    <tr>
                        <td style="background-color: #0B0D0C; padding: 20px 32px; text-align: center; border-top: 1px solid #262B28;">
                            <p style="margin: 0; font-size: 13px; color: #D4AF37; font-weight: 600;">
                                Equipo de Seguridad de Finance App
                            </p>
                            <p style="margin: 6px 0 0; font-size: 11px; color: #6B7280;">
                                Protección automatizada de cuentas.
                            </p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """
    ),

    AUTH_PASSWORD_CHANGED(
            "Alerta de Seguridad: Contraseña Actualizada - Finance App",
            """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; background-color: #0B0D0C; font-family: 'Segoe UI', Arial, sans-serif; color: #F3F4F6;">
                <table role="presentation" width="600" cellspacing="0" cellpadding="0" border="0" align="center" style="max-width: 600px; margin: 32px auto; background-color: #141816; border: 1px solid #D4AF37; border-radius: 12px; overflow: hidden;">
                    <tr>
                        <td style="background: linear-gradient(135deg, #0B0D0C 0%%, #1A241E 100%%); padding: 28px 32px; text-align: center; border-bottom: 2px solid #D4AF37;">
                            <h1 style="margin: 0; font-size: 22px; font-weight: 700; color: #F3D06B; letter-spacing: 0.5px;">FINANCE APP</h1>
                            <p style="margin: 6px 0 0; font-size: 12px; text-transform: uppercase; letter-spacing: 2px; color: #D4AF37;">Alerta de Seguridad</p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 36px 32px;">
                            <h2 style="margin: 0 0 16px; font-size: 20px; color: #FFFFFF;">Hola <span style="color: #F3D06B;">%s</span>,</h2>
                            <p style="margin: 0 0 16px; font-size: 15px; line-height: 1.6; color: #D1D5DB;">
                                Te confirmamos que la contraseña de tu cuenta en <strong style="color: #F3D06B;">Finance App</strong> ha sido actualizada exitosamente.
                            </p>
                            <p style="margin: 20px 0 0; font-size: 14px; line-height: 1.6; color: #9CA3AF;">
                                Si no realizaste este cambio, contacta de inmediato con soporte para proteger tu cuenta.
                            </p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """
    ),

    TRANSACTION_ALERT(
            "Notificación de Movimiento Financiero - Finance App",
            """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; background-color: #0B0D0C; font-family: 'Segoe UI', Arial, sans-serif; color: #F3F4F6;">
                <table role="presentation" width="600" cellspacing="0" cellpadding="0" border="0" align="center" style="max-width: 600px; margin: 32px auto; background-color: #141816; border: 1px solid #D4AF37; border-radius: 12px; overflow: hidden;">
                    <tr>
                        <td style="background: linear-gradient(135deg, #0B0D0C 0%%, #1A241E 100%%); padding: 28px 32px; text-align: center; border-bottom: 2px solid #D4AF37;">
                            <h1 style="margin: 0; font-size: 22px; font-weight: 700; color: #F3D06B; letter-spacing: 0.5px;">FINANCE APP</h1>
                            <p style="margin: 6px 0 0; font-size: 12px; text-transform: uppercase; letter-spacing: 2px; color: #10B981;">Movimiento Financiero</p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 36px 32px;">
                            <h2 style="margin: 0 0 16px; font-size: 20px; color: #FFFFFF;">Nuevo Movimiento Registrado</h2>
                            <p style="margin: 0 0 12px; font-size: 15px; color: #D1D5DB;"><strong>Tipo:</strong> %s</p>
                            <p style="margin: 0 0 12px; font-size: 15px; color: #10B981;"><strong>Monto:</strong> %s</p>
                            <p style="margin: 0 0 16px; font-size: 15px; color: #D1D5DB;"><strong>Descripción:</strong> %s</p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """
    ),

    SAVINGS_GOAL_MILESTONE(
            "¡Meta de Ahorro Alcanzada! - Finance App",
            """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; background-color: #0B0D0C; font-family: 'Segoe UI', Arial, sans-serif; color: #F3F4F6;">
                <table role="presentation" width="600" cellspacing="0" cellpadding="0" border="0" align="center" style="max-width: 600px; margin: 32px auto; background-color: #141816; border: 1px solid #D4AF37; border-radius: 12px; overflow: hidden;">
                    <tr>
                        <td style="background: linear-gradient(135deg, #0B0D0C 0%%, #1A241E 100%%); padding: 28px 32px; text-align: center; border-bottom: 2px solid #D4AF37;">
                            <h1 style="margin: 0; font-size: 22px; font-weight: 700; color: #F3D06B; letter-spacing: 0.5px;">FINANCE APP</h1>
                            <p style="margin: 6px 0 0; font-size: 12px; text-transform: uppercase; letter-spacing: 2px; color: #10B981;">Progreso de Meta de Ahorro</p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 36px 32px;">
                            <h2 style="margin: 0 0 16px; font-size: 20px; color: #FFFFFF;">¡Felicitaciones por tu avance!</h2>
                            <p style="margin: 0 0 12px; font-size: 15px; color: #F3D06B;"><strong>Meta:</strong> %s</p>
                            <p style="margin: 0 0 12px; font-size: 15px; color: #D1D5DB;"><strong>Progreso acumulado:</strong> %s</p>
                            <p style="margin: 0 0 16px; font-size: 15px; color: #10B981;"><strong>Porcentaje completado:</strong> %s</p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """
    ),

    ACCOUNT_STATUS_CHANGED(
            "Actualización de Estado de Cuenta - Finance App",
            """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; background-color: #0B0D0C; font-family: 'Segoe UI', Arial, sans-serif; color: #F3F4F6;">
                <table role="presentation" width="600" cellspacing="0" cellpadding="0" border="0" align="center" style="max-width: 600px; margin: 32px auto; background-color: #141816; border: 1px solid #D4AF37; border-radius: 12px; overflow: hidden;">
                    <tr>
                        <td style="background: linear-gradient(135deg, #0B0D0C 0%%, #1A241E 100%%); padding: 28px 32px; text-align: center; border-bottom: 2px solid #D4AF37;">
                            <h1 style="margin: 0; font-size: 22px; font-weight: 700; color: #F3D06B; letter-spacing: 0.5px;">FINANCE APP</h1>
                            <p style="margin: 6px 0 0; font-size: 12px; text-transform: uppercase; letter-spacing: 2px; color: #D4AF37;">Estado de Cuenta</p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 36px 32px;">
                            <h2 style="margin: 0 0 16px; font-size: 20px; color: #FFFFFF;">Hola <span style="color: #F3D06B;">%s</span>,</h2>
                            <p style="margin: 0 0 16px; font-size: 15px; line-height: 1.6; color: #D1D5DB;">
                                El estado de tu cuenta en <strong style="color: #F3D06B;">Finance App</strong> ha sido actualizado a: <strong style="color: #10B981;">%s</strong>.
                            </p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """
    ),

    PROJECTION_DEFICIT_ALERT(
            "Alerta Financiera: Proyección de Saldo Negativo - Finance App",
            """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; background-color: #0B0D0C; font-family: 'Segoe UI', Arial, sans-serif; color: #F3F4F6;">
                <table role="presentation" width="600" cellspacing="0" cellpadding="0" border="0" align="center" style="max-width: 600px; margin: 32px auto; background-color: #141816; border: 1px solid #D4AF37; border-radius: 12px; overflow: hidden;">
                    <tr>
                        <td style="background: linear-gradient(135deg, #0B0D0C 0%%, #1A241E 100%%); padding: 28px 32px; text-align: center; border-bottom: 2px solid #D4AF37;">
                            <h1 style="margin: 0; font-size: 22px; font-weight: 700; color: #F3D06B; letter-spacing: 0.5px;">FINANCE APP</h1>
                            <p style="margin: 6px 0 0; font-size: 12px; text-transform: uppercase; letter-spacing: 2px; color: #EF4444;">Alerta de Proyección Financiera</p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 36px 32px;">
                            <h2 style="margin: 0 0 16px; font-size: 20px; color: #FFFFFF;">Atención: Déficit Proyectado</h2>
                            <p style="margin: 0 0 12px; font-size: 15px; color: #D1D5DB;"><strong>Horizonte evaluado:</strong> %s meses</p>
                            <p style="margin: 0 0 12px; font-size: 15px; color: #EF4444;"><strong>Saldo proyectado:</strong> %s</p>
                            <p style="margin: 0 0 16px; font-size: 15px; color: #F3D06B;"><strong>Recomendación:</strong> %s</p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
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
