package com.rafaelcabanillas.sweeties.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
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

    @NotNull @Min(0)
    private Double price;

    @Builder.Default
    private List<String> materials = new ArrayList<>();

    // Optional sizes
    private List<SizeDTO> size;

    // Optional flags (defaults if null)
    private Boolean isFeatured; // if null, service/controller can default to false
    private Boolean isVisible;  // if null, service/controller can default to true
}
