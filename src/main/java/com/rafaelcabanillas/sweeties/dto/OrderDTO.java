package com.rafaelcabanillas.sweeties.dto;

import lombok.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String note;
    private List<OrderItemDTO> items;
    private Double total;
    private String status;
    private String createdAt;
    private String updatedAt;
}
