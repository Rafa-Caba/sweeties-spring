package com.rafaelcabanillas.sweeties.service;

import com.rafaelcabanillas.sweeties.dto.ContactRequestDTO;
import com.rafaelcabanillas.sweeties.dto.OrderDTO;

public interface EmailService {

    void sendOrderConfirmationToGuest(OrderDTO order);
    void sendOrderConfirmationToAdmin(OrderDTO order);
    void sendContactFormToAdmin(ContactRequestDTO contactRequest);

}