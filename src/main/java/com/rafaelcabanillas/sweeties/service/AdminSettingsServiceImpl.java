package com.rafaelcabanillas.sweeties.service;

import com.rafaelcabanillas.sweeties.dto.settings.AdminSettingsDTO;
import com.rafaelcabanillas.sweeties.dto.settings.PublicAdminSettingsDTO;
import com.rafaelcabanillas.sweeties.dto.settings.UpdateAdminSettingsDTO;
import com.rafaelcabanillas.sweeties.exception.ResourceNotFoundException;
import com.rafaelcabanillas.sweeties.model.AdminSettings;
import com.rafaelcabanillas.sweeties.repository.AdminSettingsRepository;
import com.rafaelcabanillas.sweeties.util.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSettingsServiceImpl implements AdminSettingsService {

    private final AdminSettingsRepository settingsRepository;
    private final CloudinaryService cloudinaryService;

    // The fixed ID for our singleton settings row
    private static final Long SETTINGS_ID = 1L;

    /**
     * Gets the singleton settings object.
     * Throws an exception if the row (ID=1) doesn't exist,
     * which should have been created by the Flyway migration.
     */
    private AdminSettings getSingletonInstance() {
        return settingsRepository.findById(SETTINGS_ID)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "AdminSettings singleton (ID=1) not found. Please run Flyway migration."
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminSettingsDTO getAdminSettings() {
        return toAdminDTO(getSingletonInstance());
    }

    @Override
    @Transactional(readOnly = true)
    public PublicAdminSettingsDTO getPublicSettings() {
        return toPublicDTO(getSingletonInstance());
    }

    @Override
    @Transactional
    public AdminSettingsDTO updateAdminSettings(
            UpdateAdminSettingsDTO dto,
            MultipartFile logoLight,
            MultipartFile logoDark,
            MultipartFile favicon,
            MultipartFile ogImage,
            MultipartFile aboutImage
    ) throws IOException {

        AdminSettings settings = getSingletonInstance();

        // --- 1. Apply DTO text/json fields ---
        // We check for null to support PATCH-like semantics
        updateField(dto.getSiteName(), settings::setSiteName);
        updateField(dto.getSiteTagline(), settings::setSiteTagline);
        updateField(dto.getContactEmail(), settings::setContactEmail);
        updateField(dto.getContactPhone(), settings::setContactPhone);
        updateField(dto.getContactWhatsApp(), settings::setContactWhatsApp);
        updateField(dto.getContactAddress(), settings::setContactAddress);
        updateField(dto.getDefaultThemeMode(), settings::setDefaultThemeMode);
        updateField(dto.getPublicThemeGroup(), settings::setPublicThemeGroup);
        updateField(dto.getAdminThemeGroup(), settings::setAdminThemeGroup);

        if (settings.getAbout() == null) {
            settings.setAbout(new AdminSettings.About());
        }

        // Nested objects
        if (dto.getSocial() != null) settings.setSocial(dto.getSocial());

        if (dto.getHome() != null) {
            updateField(dto.getHome().getHeroTitle(), settings.getHome()::setHeroTitle);
            updateField(dto.getHome().getHeroSubtitle(), settings.getHome()::setHeroSubtitle);
            updateField(dto.getHome().getCreatorName(), settings.getHome()::setCreatorName);
        }

        if (dto.getAbout() != null) {
            updateField(dto.getAbout().getBio(), settings.getAbout()::setBio);
        }

        if (dto.getGallery() != null) {
            updateField(dto.getGallery().getItemsPerPage(), settings.getGallery()::setItemsPerPage);
        }

        if (dto.getFooter() != null) {
            updateField(dto.getFooter().getLegalText(), settings.getFooter()::setLegalText);
            // Replace collection
            if (dto.getFooter().getAuxiliaryLinks() != null) {
                settings.getFooter().setAuxiliaryLinks(new ArrayList<>(dto.getFooter().getAuxiliaryLinks()));
            }
        }

        if (dto.getSeo() != null) {
            updateField(dto.getSeo().getSiteDescription(), settings.getSeo()::setSiteDescription);
            updateField(dto.getSeo().getOgTitle(), settings.getSeo()::setOgTitle);
            updateField(dto.getSeo().getOgDescription(), settings.getSeo()::setOgDescription);
            // Replace collection
            if (dto.getSeo().getMetaKeywords() != null) {
                settings.getSeo().setMetaKeywords(new ArrayList<>(dto.getSeo().getMetaKeywords()));
            }
        }

        // Nested objects with Boolean wrappers
        if (dto.getFeatures() != null) {
            updateField(dto.getFeatures().getEnableOrders(), settings.getFeatures()::setEnableOrders);
            updateField(dto.getFeatures().getEnableGallery(), settings.getFeatures()::setEnableGallery);
            updateField(dto.getFeatures().getEnableMaterials(), settings.getFeatures()::setEnableMaterials);
            updateField(dto.getFeatures().getEnableContactPage(), settings.getFeatures()::setEnableContactPage);
            updateField(dto.getFeatures().getEnableCart(), settings.getFeatures()::setEnableCart);
        }

        if (dto.getVisibility() != null) {
            updateField(dto.getVisibility().getShowEmail(), settings.getVisibility()::setShowEmail);
            updateField(dto.getVisibility().getShowPhone(), settings.getVisibility()::setShowPhone);
            updateField(dto.getVisibility().getShowWhatsApp(), settings.getVisibility()::setShowWhatsApp);
            updateField(dto.getVisibility().getShowAddress(), settings.getVisibility()::setShowAddress);
            updateField(dto.getVisibility().getShowSocial(), settings.getVisibility()::setShowSocial);
        }

        // --- 2. Apply File Uploads ---
        String folder = "sweeties-crochet/settings";

        // Helper lambda to handle a single file upload
        BiConsumerThrowsIOException<MultipartFile, String> uploadHandler = (file, fieldName) -> {
            if (file == null || file.isEmpty()) return;
            ensureImage(file);

            String oldPublicId = null;
            if (fieldName.equals("logoLight")) oldPublicId = settings.getLogoLightPublicId();
            else if (fieldName.equals("logoDark")) oldPublicId = settings.getLogoDarkPublicId();
            else if (fieldName.equals("favicon")) oldPublicId = settings.getFaviconPublicId();
            else if (fieldName.equals("ogImage")) oldPublicId = settings.getSeo().getOgImagePublicId();

            if (oldPublicId != null && !oldPublicId.isBlank()) {
                try { cloudinaryService.deleteFile(oldPublicId); }
                catch (Exception e) { log.warn("Failed to delete old Cloudinary file [{}]: {}", oldPublicId, e.getMessage()); }
            }

            Map<String, Object> upload;
            try {
                // Use a fixed public ID for settings images so they are overwritten
                upload = cloudinaryService.uploadFile(file, folder, "setting_" + fieldName);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloudinary upload (" + fieldName + ") failed: " + e.getMessage(), e);
            }

            String url = Objects.toString(upload.get("secure_url"), null);
            String publicId = Objects.toString(upload.get("public_id"), null);

            if (fieldName.equals("logoLight")) {
                settings.setLogoLightUrl(url);
                settings.setLogoLightPublicId(publicId);
            } else if (fieldName.equals("logoDark")) {
                settings.setLogoDarkUrl(url);
                settings.setLogoDarkPublicId(publicId);
            } else if (fieldName.equals("favicon")) {
                settings.setFaviconUrl(url);
                settings.setFaviconPublicId(publicId);
            } else if (fieldName.equals("ogImage")) {
                settings.getSeo().setOgImageUrl(url);
                settings.getSeo().setOgImagePublicId(publicId);
            } else if (fieldName.equals("aboutImage")) {
                settings.getAbout().setImageUrl(url);
                settings.getAbout().setImagePublicId(publicId);
            }
        };

        // Execute handlers for each file
        uploadHandler.accept(logoLight, "logoLight");
        uploadHandler.accept(logoDark, "logoDark");
        uploadHandler.accept(favicon, "favicon");
        uploadHandler.accept(ogImage, "ogImage");
        uploadHandler.accept(aboutImage, "aboutImage");

        // --- 3. Save and return ---
        AdminSettings updatedSettings = settingsRepository.save(settings);
        return toAdminDTO(updatedSettings);
    }

    // --- Mappers ---

    private AdminSettingsDTO toAdminDTO(AdminSettings s) {
        return AdminSettingsDTO.builder()
                .siteName(s.getSiteName())
                .siteTagline(s.getSiteTagline())
                .logoLightUrl(s.getLogoLightUrl())
                .logoLightPublicId(s.getLogoLightPublicId())
                .logoDarkUrl(s.getLogoDarkUrl())
                .logoDarkPublicId(s.getLogoDarkPublicId())
                .faviconUrl(s.getFaviconUrl())
                .faviconPublicId(s.getFaviconPublicId())
                .contactEmail(s.getContactEmail())
                .contactPhone(s.getContactPhone())
                .contactWhatsApp(s.getContactWhatsApp())
                .contactAddress(s.getContactAddress())
                .about(s.getAbout() != null ? s.getAbout() : new AdminSettings.About())
                .social(s.getSocial())
                .defaultThemeMode(s.getDefaultThemeMode())
                .publicThemeGroup(s.getPublicThemeGroup())
                .adminThemeGroup(s.getAdminThemeGroup())
                .features(s.getFeatures())
                .seo(s.getSeo())
                .visibility(s.getVisibility())
                .home(s.getHome())
                .gallery(s.getGallery())
                .footer(s.getFooter())
                .build();
    }

    private PublicAdminSettingsDTO toPublicDTO(AdminSettings s) {
        // Apply visibility rules
        AdminSettings.Visibility v = s.getVisibility();

        return PublicAdminSettingsDTO.builder()
                .siteName(s.getSiteName())
                .siteTagline(s.getSiteTagline())
                .logoLightUrl(s.getLogoLightUrl())
                .logoDarkUrl(s.getLogoDarkUrl())
                .faviconUrl(s.getFaviconUrl())
                .contactEmail(v.isShowEmail() ? s.getContactEmail() : null)
                .contactPhone(v.isShowPhone() ? s.getContactPhone() : null)
                .contactWhatsApp(v.isShowWhatsApp() ? s.getContactWhatsApp() : null)
                .contactAddress(v.isShowAddress() ? s.getContactAddress() : null)
                .about(s.getAbout() != null ? s.getAbout() : new AdminSettings.About())
                .social(v.isShowSocial() ? s.getSocial() : null)
                .defaultThemeMode(s.getDefaultThemeMode())
                .publicThemeGroup(s.getPublicThemeGroup())
                .features(s.getFeatures())
                .siteDescription(s.getSeo().getSiteDescription())
                .metaKeywords(s.getSeo().getMetaKeywords())
                .ogTitle(s.getSeo().getOgTitle())
                .ogDescription(s.getSeo().getOgDescription())
                .ogImageUrl(s.getSeo().getOgImageUrl())
                .visibility(s.getVisibility())
                .home(s.getHome())
                .gallery(s.getGallery())
                .footer(s.getFooter())
                .build();
    }

    // --- Helpers ---

    /** Helper to update a field only if the new value is not null */
    private <T> void updateField(T newValue, Consumer<T> setter) {
        if (newValue != null) {
            setter.accept(newValue);
        }
    }

    /** Functional interface for our helper lambda */
    @FunctionalInterface
    interface BiConsumerThrowsIOException<T, U> {
        void accept(T t, U u) throws IOException;
    }

    private void ensureImage(MultipartFile file) {
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image/* uploads are allowed for settings");
        }
    }
}