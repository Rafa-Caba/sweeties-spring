package com.rafaelcabanillas.sweeties.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserDTO {
    private String name;
    private String username;
    private String email;
    private String password;
    private String role;
    private String bio;
    private String imageUrl;
    private String imagePublicId;
}
