package com.rafaelcabanillas.sweeties.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @NotBlank
    private String productId;

    @NotBlank
    private String name;

    @NotNull @Min(0)
    private Double price;

    @NotNull @Min(1)
    private Integer quantity;
}
