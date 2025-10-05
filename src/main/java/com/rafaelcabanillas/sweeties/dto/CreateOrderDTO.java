package com.rafaelcabanillas.sweeties.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderDTO {
    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String phone;

    private String note;

    @NotNull
    @Size(min = 1)
    private List<OrderItemDTO> items;

    @NotNull @Min(0)
    private Double total;
}
