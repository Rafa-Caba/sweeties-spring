package com.rafaelcabanillas.sweeties.dto.settings;

import com.rafaelcabanillas.sweeties.model.AdminSettings;
import lombok.Data;

import java.util.List;

// DTO for the PUT/PATCH JSON body.
// All fields are optional (wrappers for booleans)
@Data
public class UpdateAdminSettingsDTO {
    // Branding
    private String siteName;
    private String siteTagline;

    // Contact
    private String contactEmail;
    private String contactPhone;
    private String contactWhatsApp;
    private String contactAddress;

    // Social
    private AdminSettings.Social social;

    // UI / Theme
    private AdminSettings.ThemeMode defaultThemeMode;
    private String publicThemeGroup;
    private String adminThemeGroup;

    // Features (use wrappers to allow setting 'false')
    @Data
    public static class FeaturesUpdate {
        private Boolean enableOrders;
        private Boolean enableGallery;
        private Boolean enableMaterials;
        private Boolean enableContactPage;
        private Boolean enableCart;
    }
    private FeaturesUpdate features;

    // SEO
    @Data
    public static class SeoUpdate {
        private String siteDescription;
        private List<String> metaKeywords;
        private String ogTitle;
        private String ogDescription;
        // Image fields are handled by MultipartFile, not this DTO
    }
    private SeoUpdate seo;

    // Visibility
    @Data
    public static class VisibilityUpdate {
        private Boolean showEmail;
        private Boolean showPhone;
        private Boolean showWhatsApp;
        private Boolean showAddress;
        private Boolean showSocial;
    }
    private VisibilityUpdate visibility;

    // Page Copy
    @Data
    public static class HomeUpdate {
        private String heroTitle;
        private String heroSubtitle;
        private String creatorName;
    }
    private HomeUpdate home;

    @Data
    public static class GalleryUpdate {
        private Integer itemsPerPage;
    }
    private GalleryUpdate gallery;

    @Data
    public static class FooterUpdate {
        private String legalText;
        private List<AdminSettings.AuxiliaryLink> auxiliaryLinks;
    }
    private FooterUpdate footer;
}