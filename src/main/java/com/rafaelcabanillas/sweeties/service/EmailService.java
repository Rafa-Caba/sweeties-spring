package com.rafaelcabanillas.sweeties.service;

import com.rafaelcabanillas.sweeties.dto.ContactRequestDTO;
import com.rafaelcabanillas.sweeties.dto.OrderDTO;
import jakarta.mail.MessagingException;

public interface EmailService {
    void sendOrderConfirmationToGuest(OrderDTO order) throws MessagingException;
    void sendOrderConfirmationToAdmin(OrderDTO order) throws MessagingException;

    void sendContactFormToAdmin(ContactRequestDTO contactRequest) throws MessagingException;
}