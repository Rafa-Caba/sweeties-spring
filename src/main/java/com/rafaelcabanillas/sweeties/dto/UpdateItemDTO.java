package com.rafaelcabanillas.sweeties.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateItemDTO {
    private String name;
    private String description;
    private Double price;
    private List<String> materials;
    private List<SizeDTO> size;
    private List<String> sprites;
    private List<String> spritesPublicIds;
    private boolean isFeatured;
    private boolean isVisible;
    private String imageUrl;
    private String imagePublicId;
}
