package com.rafaelcabanillas.sweeties.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContactRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(min = 10, max = 5000, message = "El mensaje debe tener entre 10 y 5000 caracteres")
    private String message;

    // Optional: a subject line if your form has one
    private String subject;
}