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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "item_materials",
            joinColumns = @JoinColumn(name = "item_id")
    )
    @Column(name = "material", nullable = false)
    @Builder.Default
    private List<String> materials = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_sizes", joinColumns = @JoinColumn(name = "item_id"))
    @Builder.Default
    private List<Size> size = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_sprites", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "sprite_url")
    @Builder.Default
    private List<String> sprites = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_sprite_public_ids", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "sprite_public_id")
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
