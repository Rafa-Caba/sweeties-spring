package com.rafaelcabanillas.sweeties.service;

import com.rafaelcabanillas.sweeties.exception.TokenRefreshException;
import com.rafaelcabanillas.sweeties.model.*;
import com.rafaelcabanillas.sweeties.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    public RefreshToken createToken(User user, long durationMinutes) {
        String token = UUID.randomUUID().toString();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(durationMinutes);
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(expiresAt)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public boolean isValid(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(rt -> rt.getExpiresAt().isAfter(OffsetDateTime.now()))
                .orElse(false);
    }

    @Override
    public User getUserFromToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> rt.getExpiresAt().isAfter(OffsetDateTime.now()))
                .map(RefreshToken::getUser)
                .orElse(null);
    }

    @Override
    @Transactional
    public void deleteToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    @Override
    @Transactional
    public RefreshToken rotateToken(String oldToken) {
        // 1. Find and validate the old token in one go
        RefreshToken rt = refreshTokenRepository.findByToken(oldToken)
                .filter(token -> token.getExpiresAt().isAfter(OffsetDateTime.now()))
                .orElseThrow(() -> new TokenRefreshException("Token inválido o expirado"));

        User user = rt.getUser();

        // 2. Delete the old one
        refreshTokenRepository.delete(rt);

        // 3. Create the new one (using the 7-day duration from your controller)
        long durationMinutes = 60 * 24 * 7;

        return this.createToken(user, durationMinutes);
    }
}
