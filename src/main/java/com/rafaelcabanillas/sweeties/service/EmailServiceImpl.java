package com.rafaelcabanillas.sweeties.service;

import com.rafaelcabanillas.sweeties.dto.ContactRequestDTO;
import com.rafaelcabanillas.sweeties.dto.OrderDTO;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
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

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final TemplateEngine templateEngine;

    // INJECT SendGrid config from environment variables
    @Value("${SENDGRID_API_KEY}")
    private String sendGridApiKey;

    @Value("${SENDGRID_FROM_EMAIL}")
    private String fromEmail;

    @Value("${sweeties.admin-email}")
    private String adminEmail;

    @Override
    @Async
    public void sendOrderConfirmationToGuest(OrderDTO order) {
        // This part is unchanged
        Context context = new Context();
        context.setVariable("order", order);
        String htmlContent = templateEngine.process("email/order-confirmation-guest", context);

        // Call the new helper
        sendHtmlEmail(
                order.getEmail(),
                "¡Confirmación de tu pedido Sweeties!",
                htmlContent,
                null // no reply-to
        );
    }

    @Override
    @Async
    public void sendOrderConfirmationToAdmin(OrderDTO order) {
        // This part is unchanged
        Context context = new Context();
        context.setVariable("order", order);
        String htmlContent = templateEngine.process("email/order-notification-admin", context);

        // Call the new helper
        sendHtmlEmail(
                adminEmail,
                "¡Nuevo Pedido Recibido! (ID: " + order.getId() + ")",
                htmlContent,
                null // no reply-to
        );
    }

    @Override
    @Async
    public void sendContactFormToAdmin(ContactRequestDTO contactRequest) {
        // This part is unchanged
        Context context = new Context();
        context.setVariable("contact", contactRequest);
        String htmlContent = templateEngine.process("email/contact-notification-admin", context);

        String subject = "Nuevo Mensaje de Contacto: " + (contactRequest.getSubject() != null ? contactRequest.getSubject() : contactRequest.getName());

        // Call the new helper
        sendHtmlEmail(
                adminEmail,
                subject,
                htmlContent,
                contactRequest.getEmail() // Set the guest's email as the reply-to
        );
    }

    /**
     * Helper method to create and send an HTML email
     */
    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        sendHtmlEmail(to, subject, htmlBody, null); // Call the overloaded helper
    }

    /**
     * NEW: Helper method using SendGrid's API
     */
    private void sendHtmlEmail(String to, String subject, String htmlBody, String replyTo) {
        // Use Email objects from SendGrid
        Email from = new Email(fromEmail);
        Email toEmail = new Email(to);
        Content content = new Content("text/html", htmlBody);
        Mail mail = new Mail(from, subject, toEmail, content);

        if (replyTo != null && !replyTo.isBlank()) {
            mail.setReplyTo(new Email(replyTo));
        }

        // Create the SendGrid client
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            // Send the request
            Response response = sg.api(request);

            // Log the result
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("SendGrid Email sent successfully to {}. Status: {}", to, response.getStatusCode());
            } else {
                // Log a warning if SendGrid fails
                log.warn("SendGrid Email failed to send to {}. Status: {}. Body: {}", to, response.getStatusCode(), response.getBody());
            }
        } catch (IOException ex) {
            // Log an error if the API call itself fails
            log.error("Error calling SendGrid API for email to {}", to, ex);
        }
    }
}