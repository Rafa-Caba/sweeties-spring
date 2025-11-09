package com.rafaelcabanillas.sweeties.controller;

import com.rafaelcabanillas.sweeties.dto.AuthResponseDTO;
import com.rafaelcabanillas.sweeties.dto.LoginRequestDTO;
import com.rafaelcabanillas.sweeties.dto.RefreshRequestDTO;
import com.rafaelcabanillas.sweeties.dto.RegisterRequestDTO;
import com.rafaelcabanillas.sweeties.model.RefreshToken;
import com.rafaelcabanillas.sweeties.model.User;
import com.rafaelcabanillas.sweeties.model.User.Role;
import com.rafaelcabanillas.sweeties.repository.UserRepository;
import com.rafaelcabanillas.sweeties.service.RefreshTokenService;
import com.rafaelcabanillas.sweeties.util.JwtUtil;
import com.rafaelcabanillas.sweeties.exception.TokenRefreshException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    // -------------------- LOGIN --------------------
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO req) {
        Optional<User> userOpt = userRepository.findByEmail(req.getEmail());
        if (userOpt.isEmpty()) {
            return unauthorized("Invalid credentials");
        }
        User user = userOpt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return unauthorized("Invalid credentials");
        }

        String accessToken = jwtUtil.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createToken(user, 60 * 24 * 7); // 7 days

        return ResponseEntity.ok(AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .role(user.getRole().name())
                .build());
    }

    // -------------------- LOGOUT --------------------
    @PostMapping(value = "/logout", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshRequestDTO req) {
        // Idempotent: deleting a non-existent token is fine
        refreshTokenService.deleteToken(req.getRefreshToken());
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    // -------------------- REGISTER --------------------
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO req) {
        // Uniqueness checks (case-insensitive username handling on our side)
        Optional<User> byEmail = userRepository.findByEmail(req.getEmail());
        if (byEmail.isPresent()) {
            return conflict("Email already in use");
        }
        Optional<User> byUsername = userRepository.findByUsername(req.getUsername().toLowerCase());
        if (byUsername.isPresent()) {
            return conflict("Username already in use");
        }

        // Create user (normalize username to lowercase)
        User user = User.builder()
                .name(req.getName())
                .username(req.getUsername().toLowerCase())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.ADMIN) // TODO: change to desired default (ADMIN/GUEST/VIEWER) or from req
                .build();

        userRepository.save(user);

        String accessToken = jwtUtil.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createToken(user, 60 * 24 * 7);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponseDTO.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken.getToken())
                        .role(user.getRole().name())
                        .build());
    }

    // -------------------- REFRESH (ROTATE) --------------------
    @PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequestDTO req) {

        try {
            // 1. Call the single service method
            RefreshToken newRefreshToken = refreshTokenService.rotateToken(req.getRefreshToken());

            // 2. Get the user from the result
            User user = newRefreshToken.getUser();

            // 3. Generate the new access token
            String newAccessToken = jwtUtil.generateToken(user);

            return ResponseEntity.ok(AuthResponseDTO.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken.getToken())
                    .role(user.getRole().name())
                    .build());
        } catch (TokenRefreshException e) {
            return unauthorized(e.getMessage());
        }
    }

    // -------------------- Helpers --------------------
    private ResponseEntity<Map<String, Object>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Unauthorized", "message", message));
    }

    private ResponseEntity<Map<String, Object>> conflict(String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Conflict", "message", message));
    }
}
