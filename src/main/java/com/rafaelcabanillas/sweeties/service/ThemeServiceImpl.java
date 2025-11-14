package com.rafaelcabanillas.sweeties.service;

import com.rafaelcabanillas.sweeties.dto.ThemeDTO;
import com.rafaelcabanillas.sweeties.dto.UserDTO;
import com.rafaelcabanillas.sweeties.exception.ResourceNotFoundException;
import com.rafaelcabanillas.sweeties.model.Theme;
import com.rafaelcabanillas.sweeties.model.User;
import com.rafaelcabanillas.sweeties.repository.ThemeRepository;
import com.rafaelcabanillas.sweeties.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ThemeServiceImpl implements ThemeService {

    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;

    @Override
    public List<ThemeDTO> getAllThemes() {
        return themeRepository.findAll().stream()
                .map(this::toThemeDTO)
                .toList();
    }

    @Override
    @Transactional
    public UserDTO updateUserTheme(String username, Long themeId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tema no encontrado"));

        user.setTheme(theme);
        userRepository.save(user);

        return toUserDTO(user);
    }

    // --- Mappers ---

    private ThemeDTO toThemeDTO(Theme theme) {
        return ThemeDTO.builder()
                .id(theme.getId())
                .name(theme.getName())
                .isDark(theme.isDark())
                .primaryColor(theme.getPrimaryColor())
                .accentColor(theme.getAccentColor())
                .backgroundColor(theme.getBackgroundColor())
                .textColor(theme.getTextColor())
                .cardColor(theme.getCardColor())
                .buttonColor(theme.getButtonColor())
                .navColor(theme.getNavColor())
                .build();
    }

    // NOTE: You likely already have this in UserServiceImpl.
    // Ideally, you should share a single UserMapper component,
    // but for now we can duplicate the simple mapping here or inject UserService.
    private UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .bio(user.getBio())
                .imageUrl(user.getImageUrl())
                .imagePublicId(user.getImagePublicId())
                // Map the Theme ID if it exists
                .themeId(user.getTheme() != null ? user.getTheme().getId() : null)
                .build();
    }
}