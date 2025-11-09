package com.rafaelcabanillas.sweeties.service;

import com.rafaelcabanillas.sweeties.dto.ContactRequestDTO;
import com.rafaelcabanillas.sweeties.dto.OrderDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${sweeties.admin-email}")
    private String adminEmail;

    @Override
    @Async
    public void sendOrderConfirmationToGuest(OrderDTO order) throws MessagingException {
        // 1. Create the Thymeleaf context
        Context context = new Context();
        context.setVariable("order", order);

        // 2. Process the template to get the HTML string
        String htmlContent = templateEngine.process("email/order-confirmation-guest", context);

        // 3. Send the HTML email
        sendHtmlEmail(order.getEmail(), "¡Confirmación de tu pedido Sweeties!", htmlContent);
    }

    @Override
    @Async
    public void sendOrderConfirmationToAdmin(OrderDTO order) throws MessagingException {
        // 1. Create context
        Context context = new Context();
        context.setVariable("order", order);

        // 2. Process template
        String htmlContent = templateEngine.process("email/order-notification-admin", context);

        // 3. Send email
        sendHtmlEmail(adminEmail, "¡Nuevo Pedido Recibido! (ID: " + order.getId() + ")", htmlContent);
    }

    @Override
    @Async
    public void sendContactFormToAdmin(ContactRequestDTO contactRequest) throws MessagingException {
        // 1. Create context for the template
        Context context = new Context();
        context.setVariable("contact", contactRequest);

        // 2. Process the new template
        String htmlContent = templateEngine.process("email/contact-notification-admin", context);

        // 3. Send the email to the admin
        String subject = "Nuevo Mensaje de Contacto: " + (contactRequest.getSubject() != null ? contactRequest.getSubject() : contactRequest.getName());

        // We use 'fromEmail' as the sender, but set the 'Reply-To' header
        // to the guest's email so the admin can just click "Reply".
        sendHtmlEmail(adminEmail, subject, htmlContent, contactRequest.getEmail());
    }

    /**
     * Helper method to create and send an HTML email
     */
    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        sendHtmlEmail(to, subject, htmlBody, null); // Call the overloaded helper
    }

    /**
     * Overloaded helper method that includes an optional 'replyTo' address
     */
    private void sendHtmlEmail(String to, String subject, String htmlBody, String replyTo) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // 'true' indicates this is HTML

        if (replyTo != null && !replyTo.isBlank()) {
            helper.setReplyTo(replyTo); // <-- This is the magic
        }

        mailSender.send(mimeMessage);
        log.info("HTML Email sent successfully to {}", to);
    }
}