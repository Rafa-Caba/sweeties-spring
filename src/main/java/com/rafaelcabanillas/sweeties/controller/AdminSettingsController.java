package com.rafaelcabanillas.sweeties.controller;

import com.rafaelcabanillas.sweeties.dto.settings.AdminSettingsDTO;
import com.rafaelcabanillas.sweeties.dto.settings.PublicAdminSettingsDTO;
import com.rafaelcabanillas.sweeties.dto.settings.UpdateAdminSettingsDTO;
import com.rafaelcabanillas.sweeties.service.AdminSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
// Matches your Node.js base route: app.use('/api/admin/admin-settings', ...)
@RequestMapping("/api/admin/admin-settings")
@RequiredArgsConstructor
public class AdminSettingsController {

    private final AdminSettingsService adminSettingsService;

    /**
     * GET (Public) – projected doc
     * Matches: GET /api/admin/admin-settings/public
     */
    @GetMapping(value = "/public", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicAdminSettingsDTO> getPublicSettings() {
        return ResponseEntity.ok(adminSettingsService.getPublicSettings());
    }

    /**
     * GET (Admin) – full doc
     * Matches: GET /api/admin/admin-settings/
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminSettingsDTO> getAdminSettings() {
        return ResponseEntity.ok(adminSettingsService.getAdminSettings());
    }

    /**
     * PUT (Admin) – update + image management
     * Matches: PUT /api/admin/admin-settings/
     */
    @PutMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminSettingsDTO> updateAdminSettings(
            @Valid @RequestPart("settings") UpdateAdminSettingsDTO settingsDTO,
            @RequestPart(value = "logoLight", required = false) MultipartFile logoLight,
            @RequestPart(value = "logoDark", required = false) MultipartFile logoDark,
            @RequestPart(value = "favicon", required = false) MultipartFile favicon,
            @RequestPart(value = "ogImage", required = false) MultipartFile ogImage
    ) throws IOException {

        AdminSettingsDTO updated = adminSettingsService.updateAdminSettings(
                settingsDTO, logoLight, logoDark, favicon, ogImage
        );
        return ResponseEntity.ok(updated);
    }
}