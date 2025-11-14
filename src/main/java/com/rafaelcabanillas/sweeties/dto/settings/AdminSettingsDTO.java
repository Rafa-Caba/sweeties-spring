package com.rafaelcabanillas.sweeties.dto.settings;

import com.rafaelcabanillas.sweeties.model.AdminSettings;
import lombok.Builder;
import lombok.Data;

// Full DTO for admins (mirrors the entity structure)
@Data
@Builder
public class AdminSettingsDTO {
    // Branding
    private String siteName;
    private String siteTagline;
    private String logoLightUrl;
    private String logoLightPublicId;
    private String logoDarkUrl;
    private String logoDarkPublicId;
    private String faviconUrl;
    private String faviconPublicId;

    // Contact
    private String contactEmail;
    private String contactPhone;
    private String contactWhatsApp;
    private String contactAddress;

    // About
    private AdminSettings.About about;

    // Social
    private AdminSettings.Social social;

    // UI / Theme
    private AdminSettings.ThemeMode defaultThemeMode;
    private String publicThemeGroup;
    private String adminThemeGroup;

    // Features
    private AdminSettings.Features features;

    // SEO
    private AdminSettings.Seo seo;

    // Visibility
    private AdminSettings.Visibility visibility;

    // Page Copy
    private AdminSettings.Home home;
    private AdminSettings.Gallery gallery;
    private AdminSettings.Footer footer;
}