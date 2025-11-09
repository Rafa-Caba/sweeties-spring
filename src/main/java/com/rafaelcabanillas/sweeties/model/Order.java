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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @NotNull @Min(0)
    private Double total;

    @Enumerated(EnumType.STRING) // Tells JPA to save as "PENDIENTE", "ENVIADO", etc.
    @Column(nullable = false, length = 20) // Good to add
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDIENTE;

    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public enum OrderStatus { PENDIENTE, ENVIADO, ENTREGADO }

    @PreUpdate
    public void onUpdate() { this.updatedAt = OffsetDateTime.now(); }
}
