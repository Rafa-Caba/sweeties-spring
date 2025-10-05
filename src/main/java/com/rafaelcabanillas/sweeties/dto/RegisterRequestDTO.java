package com.rafaelcabanillas.sweeties.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String username;
    @Email
    private String email;
    private String password;
}