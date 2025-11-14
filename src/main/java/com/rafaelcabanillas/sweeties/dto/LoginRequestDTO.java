package com.rafaelcabanillas.sweeties.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @NotBlank(message = "Username or email is required")
    private String username; // This will hold either the username OR the email

    @NotBlank(message = "Password is required")
    private String password;
}