package com.rafaelcabanillas.sweeties.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private String imagePublicId;
    private List<String> materials;
    private List<SizeDTO> size;
    private List<String> sprites;
    private List<String> spritesPublicIds;

    @JsonProperty("isFeatured")
    private boolean isFeatured;

    @JsonProperty("isVisible")
    private boolean isVisible;

    // Virtuals for frontend naming
    public String getInfo() { return description; }
    public boolean isAvailable() { return isVisible; }
}
