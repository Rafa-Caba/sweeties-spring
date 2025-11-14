package com.rafaelcabanillas.sweeties.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "themes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., "Light", "Dark", "Candy"

    @Builder.Default
    private boolean isDark = false; // To help UI toggle logic

    // --- Core Colors ---
    private String primaryColor;    // #a88ff7
    private String accentColor;     // #673ab7
    private String backgroundColor; // #fefefe
    private String textColor;       // #2a2a2a
    private String cardColor;       // #ffffff
    private String buttonColor;     // #6a0dad
    private String navColor;        // rgba(...)
}