package com.finance.notification.infrastructure.adapters.mail;

import com.finance.notification.domain.gateway.NotificationSenderGateway;
import com.finance.notification.domain.model.Notification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        if (senderEmail != null && !senderEmail.isBlank()) {
            mailMessage.setFrom(senderEmail);
        }
        mailMessage.setTo(notification.recipient());
        mailMessage.setSubject(notification.subject());
        mailMessage.setText(notification.body());

        javaMailSender.send(mailMessage);
    }
}
