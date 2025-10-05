package com.rafaelcabanillas.sweeties.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateItemDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    @Min(0)
    private Double price;
    private List<String> materials;
    private List<SizeDTO> size;
    private boolean isFeatured;
    private boolean isVisible;
}
