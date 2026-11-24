package com.finance.notification.infrastructure.adapters.mail;

import com.finance.notification.domain.model.Notification;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleMailSenderAdapterTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Test
    void shouldSendMimeMessageSuccessfullyWhenSenderEmailIsConfigured() throws Exception {
        // Arrange
        GoogleMailSenderAdapter adapter = new GoogleMailSenderAdapter(javaMailSender, "noreply@finance.com");
        Notification notification = new Notification("user@finance.com", "Subject Test", "<h1>Hello</h1>");
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        adapter.send(notification);

        // Assert
        verify(javaMailSender).send(mimeMessage);
        assertEquals("Subject Test", mimeMessage.getSubject());
    }

    @Test
    void shouldSendMimeMessageSuccessfullyWhenSenderEmailIsBlank() throws Exception {
        // Arrange
        GoogleMailSenderAdapter adapter = new GoogleMailSenderAdapter(javaMailSender, "   ");
        Notification notification = new Notification("user@finance.com", "Subject Blank From", "<p>Body</p>");
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        adapter.send(notification);

        // Assert
        verify(javaMailSender).send(mimeMessage);
        assertEquals("Subject Blank From", mimeMessage.getSubject());
    }

    @Test
    void shouldSendMimeMessageSuccessfullyWhenSenderEmailIsNull() throws Exception {
        // Arrange
        GoogleMailSenderAdapter adapter = new GoogleMailSenderAdapter(javaMailSender, null);
        Notification notification = new Notification("user@finance.com", "Subject Null From", "<p>Body</p>");
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        adapter.send(notification);

        // Assert
        verify(javaMailSender).send(mimeMessage);
        assertEquals("Subject Null From", mimeMessage.getSubject());
    }

    @Test
    void shouldTranslateMessagingExceptionToIllegalStateExceptionWhenMimeMessageFails() throws Exception {
        // Arrange
        GoogleMailSenderAdapter adapter = new GoogleMailSenderAdapter(javaMailSender, "noreply@finance.com");
        Notification notification = new Notification("user@finance.com", "Subject Error", "<p>Error</p>");
        MimeMessage brokenMimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(brokenMimeMessage);
        doThrow(new MessagingException("SMTP MIME failure")).when(brokenMimeMessage).setSubject("Subject Error", "UTF-8");

        // Act
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> adapter.send(notification)
        );

        // Assert
        assertTrue(exception.getMessage().contains("Error al construir el correo HTML para: user@finance.com"));
        assertInstanceOf(MessagingException.class, exception.getCause());
    }

    @Test
    void shouldCatchMailSendAndMailAuthenticationExceptionsWithoutEscalatingUnhandled500() {
        // Arrange
        GoogleMailSenderAdapter adapter = new GoogleMailSenderAdapter(javaMailSender, "noreply@finance.com");
        Notification notification = new Notification("user@finance.com", "Subject Fail", "<p>Body</p>");
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("SMTP server down"))
                .doThrow(new MailAuthenticationException("Invalid SMTP credentials"))
                .when(javaMailSender).send(mimeMessage);

        // Act
        IllegalStateException sendEx = assertThrows(
                IllegalStateException.class,
                () -> adapter.send(notification)
        );
        IllegalStateException authEx = assertThrows(
                IllegalStateException.class,
                () -> adapter.send(notification)
        );

        // Assert
        assertTrue(sendEx.getMessage().contains("Error al enviar el correo electrónico a: user@finance.com"));
        assertInstanceOf(MailSendException.class, sendEx.getCause());
        assertTrue(authEx.getMessage().contains("Error al enviar el correo electrónico a: user@finance.com"));
        assertInstanceOf(MailAuthenticationException.class, authEx.getCause());
    }
}
