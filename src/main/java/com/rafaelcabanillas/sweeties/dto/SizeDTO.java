package com.rafaelcabanillas.sweeties.dto;

import jakarta.validation.constraints.Min;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SizeDTO {
    @Min(0) private Double alto;
    @Min(0) private Double ancho;
}
