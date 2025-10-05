package com.rafaelcabanillas.sweeties.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @Email
    private String email;
    private String password;
}