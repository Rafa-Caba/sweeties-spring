package com.rafaelcabanillas.sweeties.controller;

import com.rafaelcabanillas.sweeties.dto.ContactRequestDTO;
import com.rafaelcabanillas.sweeties.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Slf4j
public class PublicController {

    private final EmailService emailService;

    @PostMapping("/contact")
    public ResponseEntity<Map<String, String>> submitContactForm(@Valid @RequestBody ContactRequestDTO contactRequest) {

        try {
            // The email service is @Async, so this will return instantly
            emailService.sendContactFormToAdmin(contactRequest);
        } catch (Exception e) {
            // This would catch a rare error during the @Async setup,
            // NOT the email sending itself.
            log.error("Failed to enqueue contact form email: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo enviar el mensaje.");
        }

        return ResponseEntity.ok(Map.of("message", "Mensaje recibido. ¡Gracias por contactarnos!"));
    }
}