package com.rafaelcabanillas.sweeties.service;

import com.rafaelcabanillas.sweeties.dto.settings.AdminSettingsDTO;
import com.rafaelcabanillas.sweeties.dto.settings.PublicAdminSettingsDTO;
import com.rafaelcabanillas.sweeties.dto.settings.UpdateAdminSettingsDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AdminSettingsService {

    // GET (Admin)
    AdminSettingsDTO getAdminSettings();

    // GET (Public)
    PublicAdminSettingsDTO getPublicSettings();

    // PUT (Admin)
    AdminSettingsDTO updateAdminSettings(
            UpdateAdminSettingsDTO dto,
            MultipartFile logoLight,
            MultipartFile logoDark,
            MultipartFile favicon,
            MultipartFile ogImage
    ) throws IOException;
}