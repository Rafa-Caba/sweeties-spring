package com.rafaelcabanillas.sweeties.controller;

import com.rafaelcabanillas.sweeties.dto.UserDTO;
import com.rafaelcabanillas.sweeties.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    // Anyone authenticated can hit this
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> me(Authentication auth) {
        String username = auth.getName(); // subject from JWT (we set it to username)
        return ResponseEntity.ok(userService.getByUsername(username));
    }
}
