package com.rafaelcabanillas.sweeties.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateItemDTO {
    // All optional for PATCH/PUT semantics
    private String name;
    private String description;
    private Double price;
    private List<String> materials;
    private List<SizeDTO> size;

    // If the controller wants to pass sprites through the DTO on update (we do):
    private List<String> sprites;
    private List<String> spritesPublicIds;

    // Use wrappers so null = "don’t change"
    private Boolean isFeatured;
    private Boolean isVisible;

    // Convenience primitive getters (optional) if your code still calls dto.isVisible() etc.
    public boolean isFeatured() {
        return Boolean.TRUE.equals(isFeatured);
    }
    public boolean isVisible() {
        return isVisible == null || Boolean.TRUE.equals(isVisible);
    }
}
