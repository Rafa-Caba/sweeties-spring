package com.rafaelcabanillas.sweeties.service;

import com.rafaelcabanillas.sweeties.dto.OrderDTO;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Override
    public void sendOrderConfirmationToGuest(String to, OrderDTO order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Confirmación de Pedido Sweeties");
        message.setText("¡Gracias por tu pedido, " + order.getName() + "!\n" +
                "Total: $" + order.getTotal() + "\n" +
                "Te avisaremos cuando se envíe.\n");
        mailSender.send(message);
    }

    @Override
    public void sendOrderConfirmationToAdmin(String to, OrderDTO order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Nuevo Pedido recibido en Sweeties");
        message.setText("Nuevo pedido de: " + order.getName() + "\nTotal: $" + order.getTotal());
        mailSender.send(message);
    }
}
