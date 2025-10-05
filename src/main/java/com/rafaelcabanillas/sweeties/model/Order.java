package com.rafaelcabanillas.sweeties.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String phone;

    private String note;

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @NotNull @Min(0)
    private Double total;

    @NotBlank
    @Builder.Default
    private String status = "pendiente"; // pendiente | enviado | entregado

    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    public void onUpdate() { this.updatedAt = OffsetDateTime.now(); }
}
