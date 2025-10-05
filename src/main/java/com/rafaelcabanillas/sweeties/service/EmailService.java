package com.rafaelcabanillas.sweeties.service;

import com.rafaelcabanillas.sweeties.dto.OrderDTO;

public interface EmailService {
    void sendOrderConfirmationToGuest(String to, OrderDTO order);
    void sendOrderConfirmationToAdmin(String to, OrderDTO order);
}
