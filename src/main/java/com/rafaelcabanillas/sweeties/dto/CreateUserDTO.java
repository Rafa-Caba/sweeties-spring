package com.rafaelcabanillas.sweeties.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserDTO {
    @NotBlank private String name;
    @NotBlank private String username;
    @NotBlank private String email;
    @NotBlank private String password;
    private String role;
    private String bio;
}
