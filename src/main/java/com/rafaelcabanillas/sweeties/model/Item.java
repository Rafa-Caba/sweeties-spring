package com.rafaelcabanillas.sweeties.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Min(0)
    private Double price;

    @NotBlank
    private String imageUrl;

    private String imagePublicId;

    @ElementCollection
    @Builder.Default
    private List<String> materials = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "item_sizes", joinColumns = @JoinColumn(name = "item_id"))
    @Builder.Default
    private List<Size> size = new ArrayList<>();

    @ElementCollection
    @Builder.Default
    private List<String> sprites = new ArrayList<>();

    @ElementCollection
    @Builder.Default
    private List<String> spritesPublicIds = new ArrayList<>();

    @Builder.Default
    private boolean isFeatured = false;

    @Builder.Default
    private boolean isVisible = true;

    // Info and available are handled as DTO/virtuals for frontend if needed

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Size {
        @Min(0)
        private Double alto;
        @Min(0)
        private Double ancho;
    }
}
