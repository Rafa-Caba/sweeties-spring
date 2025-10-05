package com.rafaelcabanillas.sweeties.service;

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
}
