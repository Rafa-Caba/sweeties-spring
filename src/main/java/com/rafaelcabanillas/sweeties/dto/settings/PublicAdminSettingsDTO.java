package com.rafaelcabanillas.sweeties.dto.settings;

import com.rafaelcabanillas.sweeties.model.AdminSettings;
import lombok.Builder;
import lombok.Data;

// DTO for public view (the 'projection')
@Data
@Builder
public class PublicAdminSettingsDTO {
    // Branding
    private String siteName;
    private String siteTagline;
    private String logoLightUrl;
    private String logoDarkUrl;
    private String faviconUrl;

    // Contact (respecting visibility flags)
    private String contactEmail;
    private String contactPhone;
    private String contactWhatsApp;
    private String contactAddress;

    // Social
    private AdminSettings.Social social;

    // UI / Theme
    private AdminSettings.ThemeMode defaultThemeMode;
    private String publicThemeGroup;

    // Features
    private AdminSettings.Features features;

    // SEO (only public parts)
    private String siteDescription;
    private java.util.List<String> metaKeywords;
    private String ogTitle;
    private String ogDescription;
    private String ogImageUrl;

    // Visibility
    private AdminSettings.Visibility visibility;

    // Page Copy
    private AdminSettings.Home home;
    private AdminSettings.Gallery gallery;
    private AdminSettings.Footer footer;
}