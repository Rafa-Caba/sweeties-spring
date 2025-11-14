package com.rafaelcabanillas.sweeties.service;

import com.rafaelcabanillas.sweeties.dto.ThemeDTO;
import com.rafaelcabanillas.sweeties.dto.UserDTO;
import java.util.List;

public interface ThemeService {
    List<ThemeDTO> getAllThemes();
    UserDTO updateUserTheme(String username, Long themeId);
}