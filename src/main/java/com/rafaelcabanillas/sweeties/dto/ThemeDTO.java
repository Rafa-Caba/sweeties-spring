package com.rafaelcabanillas.sweeties.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeDTO {
    private Long id;
    private String name;
    private boolean isDark;

    // Colors
    private String primaryColor;
    private String accentColor;
    private String backgroundColor;
    private String textColor;
    private String cardColor;
    private String buttonColor;
    private String navColor;
}