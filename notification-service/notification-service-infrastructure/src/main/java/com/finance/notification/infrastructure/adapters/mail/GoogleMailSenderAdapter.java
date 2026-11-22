package com.finance.notification.infrastructure.adapters.mail;

import com.finance.notification.domain.gateway.NotificationSenderGateway;
import com.finance.notification.domain.model.Notification;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class GoogleMailSenderAdapter implements NotificationSenderGateway {

    private final JavaMailSender javaMailSender;
    private final String senderEmail;

    public GoogleMailSenderAdapter(
            JavaMailSender javaMailSender,
            @Value("${spring.mail.username:}") String senderEmail
    ) {
        this.javaMailSender = javaMailSender;
        this.senderEmail = senderEmail;
    }

    @Override
    public void send(Notification notification) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            if (senderEmail != null && !senderEmail.isBlank()) {
                helper.setFrom(senderEmail);
            }
            helper.setTo(notification.recipient());
            helper.setSubject(notification.subject());
            helper.setText(notification.body(), true);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("Error al construir el correo HTML para: " + notification.recipient(), e);
        }
    }
}