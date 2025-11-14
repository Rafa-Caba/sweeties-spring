package com.rafaelcabanillas.sweeties.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "admin_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSettings {

    @Id
    private Long id;

    // --- Branding ---
    @Column(nullable = false)
    private String siteName;
    private String siteTagline;

    private String logoLightUrl;
    private String logoLightPublicId;
    private String logoDarkUrl;
    private String logoDarkPublicId;
    private String faviconUrl;
    private String faviconPublicId;

    // --- Contact ---
    private String contactEmail;
    private String contactPhone;
    private String contactWhatsApp;
    private String contactAddress;

    @Embedded
    @Builder.Default
    @AttributeOverrides({
            @AttributeOverride(name = "bio", column = @Column(name = "about_bio", columnDefinition = "TEXT")),
            @AttributeOverride(name = "imageUrl", column = @Column(name = "about_image_url")),
            @AttributeOverride(name = "imagePublicId", column = @Column(name = "about_image_public_id"))
    })
    private About about = new About();

    // --- Embeddables for nested objects ---
    // We use @AttributeOverrides to be explicit about column names

    @Embedded
    @Builder.Default
    @AttributeOverrides({
            @AttributeOverride(name = "facebook", column = @Column(name = "social_facebook")),
            @AttributeOverride(name = "instagram", column = @Column(name = "social_instagram")),
            @AttributeOverride(name = "tiktok", column = @Column(name = "social_tiktok")),
            @AttributeOverride(name = "youtube", column = @Column(name = "social_youtube")),
            @AttributeOverride(name = "threads", column = @Column(name = "social_threads")),
            @AttributeOverride(name = "x", column = @Column(name = "social_x"))
    })
    private Social social = new Social();

    @Embedded
    @Builder.Default
    @AttributeOverrides({
            @AttributeOverride(name = "enableOrders", column = @Column(name = "features_enable_orders")),
            @AttributeOverride(name = "enableGallery", column = @Column(name = "features_enable_gallery")),
            @AttributeOverride(name = "enableMaterials", column = @Column(name = "features_enable_materials")),
            @AttributeOverride(name = "enableContactPage", column = @Column(name = "features_enable_contact_page")),
            @AttributeOverride(name = "enableCart", column = @Column(name = "features_enable_cart"))
    })
    private Features features = new Features();

    @Embedded
    @Builder.Default
    @AttributeOverrides({
            @AttributeOverride(name = "siteDescription", column = @Column(name = "seo_site_description", columnDefinition = "TEXT")),
            @AttributeOverride(name = "ogTitle", column = @Column(name = "seo_og_title")),
            @AttributeOverride(name = "ogDescription", column = @Column(name = "seo_og_description", columnDefinition = "TEXT")),
            @AttributeOverride(name = "ogImageUrl", column = @Column(name = "seo_og_image_url")),
            @AttributeOverride(name = "ogImagePublicId", column = @Column(name = "seo_og_image_public_id"))
    })
    private Seo seo = new Seo();

    @Embedded
    @Builder.Default
    @AttributeOverrides({
            @AttributeOverride(name = "showEmail", column = @Column(name = "visibility_show_email")),
            @AttributeOverride(name = "showPhone", column = @Column(name = "visibility_show_phone")),
            @AttributeOverride(name = "showWhatsApp", column = @Column(name = "visibility_show_whats_app")),
            @AttributeOverride(name = "showAddress", column = @Column(name = "visibility_show_address")),
            @AttributeOverride(name = "showSocial", column = @Column(name = "visibility_show_social"))
    })
    private Visibility visibility = new Visibility();

    @Embedded
    @Builder.Default
    @AttributeOverrides({
            @AttributeOverride(name = "heroTitle", column = @Column(name = "home_hero_title")),
            @AttributeOverride(name = "heroSubtitle", column = @Column(name = "home_hero_subtitle")),
            @AttributeOverride(name = "creatorName", column = @Column(name = "home_creator_name"))
    })
    private Home home = new Home();

    @Embedded
    @Builder.Default
    @AttributeOverrides({
            @AttributeOverride(name = "itemsPerPage", column = @Column(name = "gallery_items_per_page"))
    })
    private Gallery gallery = new Gallery();

    @Embedded
    @Builder.Default
    @AttributeOverrides({
            @AttributeOverride(name = "legalText", column = @Column(name = "footer_legal_text"))
    })
    private Footer footer = new Footer();

    // --- UI / Theme ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ThemeMode defaultThemeMode = ThemeMode.SYSTEM;

    private String publicThemeGroup;
    private String adminThemeGroup;

    // --- Timestamps ---
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // --- Enums ---
    public enum ThemeMode { SYSTEM, LIGHT, DARK }

    // --- Embeddable Classes ---

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Social {
        private String facebook;
        private String instagram;
        private String tiktok;
        private String youtube;
        private String threads;
        private String x; // twitter
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Features {
        @Builder.Default private boolean enableOrders = true;
        @Builder.Default private boolean enableGallery = true;
        @Builder.Default private boolean enableMaterials = true;
        @Builder.Default private boolean enableContactPage = true;
        @Builder.Default private boolean enableCart = true;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Seo {
        private String siteDescription;

        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(name = "admin_settings_meta_keywords", joinColumns = @JoinColumn(name = "admin_settings_id"))
        @Column(name = "meta_keyword")
        @Builder.Default
        @BatchSize(size = 20)
        private List<String> metaKeywords = new ArrayList<>();

        private String ogTitle;
        private String ogDescription;
        private String ogImageUrl;
        private String ogImagePublicId;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Visibility {
        @Builder.Default private boolean showEmail = true;
        @Builder.Default private boolean showPhone = false;
        @Builder.Default private boolean showWhatsApp = true;
        @Builder.Default private boolean showAddress = false;
        @Builder.Default private boolean showSocial = true;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Home {
        private String heroTitle;
        private String heroSubtitle;
        private String creatorName;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Gallery {
        @Builder.Default private Integer itemsPerPage = 10;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Footer {
        private String legalText;

        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(name = "admin_settings_aux_links", joinColumns = @JoinColumn(name = "admin_settings_id"))
        @Builder.Default
        @BatchSize(size = 10)
        private List<AuxiliaryLink> auxiliaryLinks = new ArrayList<>();
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuxiliaryLink {
        private String label;
        private String url;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class About {
        @Column(columnDefinition = "TEXT")
        private String bio;
        private String imageUrl;
        private String imagePublicId;
    }
}