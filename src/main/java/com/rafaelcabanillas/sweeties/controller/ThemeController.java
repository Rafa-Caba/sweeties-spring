package com.rafaelcabanillas.sweeties.controller;

import com.rafaelcabanillas.sweeties.dto.ThemeDTO;
import com.rafaelcabanillas.sweeties.dto.UserDTO;
import com.rafaelcabanillas.sweeties.service.ThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/themes")
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    /**
     * GET /api/themes
     * Public endpoint to get all available themes
     */
    @GetMapping
    public ResponseEntity<List<ThemeDTO>> getAllThemes() {
        return ResponseEntity.ok(themeService.getAllThemes());
    }

    /**
     * PUT /api/themes/me
     * Authenticated endpoint for user to set their own theme
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> setMyTheme(
            @RequestBody Map<String, Long> payload,
            Authentication auth
    ) {
        Long themeId = payload.get("themeId");
        String username = auth.getName(); // From JWT

        return ResponseEntity.ok(themeService.updateUserTheme(username, themeId));
    }
}