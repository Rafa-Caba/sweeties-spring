package com.rafaelcabanillas.sweeties.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TrackOrderDTO {
    @NotNull(message = "El ID del pedido es obligatorio")
    private Long orderId;

    @NotNull(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;
}