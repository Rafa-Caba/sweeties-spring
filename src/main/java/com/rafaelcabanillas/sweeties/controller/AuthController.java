package com.rafaelcabanillas.sweeties.controller;

import com.rafaelcabanillas.sweeties.model.User.Role;

import com.rafaelcabanillas.sweeties.dto.*;
import com.rafaelcabanillas.sweeties.model.User;
import com.rafaelcabanillas.sweeties.model.RefreshToken;
import com.rafaelcabanillas.sweeties.repository.UserRepository;
import com.rafaelcabanillas.sweeties.service.RefreshTokenService;
import com.rafaelcabanillas.sweeties.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    // 1. LOGIN
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO req) {
        Optional<User> userOpt = userRepository.findByEmail(req.getEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        User user = userOpt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String accessToken = jwtUtil.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createToken(user, 60 * 24 * 7); // 7 days

        return ResponseEntity.ok(AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .role(user.getRole().name())
                .build());
    }

    // 2. REFRESH TOKEN
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshRequestDTO req) {
        String refreshToken = req.getRefreshToken();
        if (!refreshTokenService.isValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        User user = refreshTokenService.getUserFromToken(refreshToken);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        String newAccessToken = jwtUtil.generateToken(user);
        RefreshToken newRefreshToken = refreshTokenService.createToken(user, 60 * 24 * 7);
        refreshTokenService.deleteToken(refreshToken); // Rotate token

        return ResponseEntity.ok(AuthResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .role(user.getRole().name())
                .build());
    }

    // 3. LOGOUT
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshRequestDTO req) {
        refreshTokenService.deleteToken(req.getRefreshToken());
        return ResponseEntity.ok().body(new MessageResponseDTO("Logout successful"));
    }

    // 4. REGISTER (optional, you can remove if not needed)
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO req) {
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null);
        }
        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.ADMIN) // Or allow setting via req, up to you
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

    // Helper DTO for logout
    @lombok.Data
    @lombok.AllArgsConstructor
    static class MessageResponseDTO {
        private String message;
    }
}
