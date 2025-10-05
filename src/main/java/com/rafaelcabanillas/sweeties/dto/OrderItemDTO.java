package com.rafaelcabanillas.sweeties.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {
    private String productId;
    private String name;
    private Double price;
    private Integer quantity;
}
