package com.rafaelcabanillas.sweeties.service;

import com.rafaelcabanillas.sweeties.model.User;
import com.rafaelcabanillas.sweeties.model.RefreshToken;

public interface RefreshTokenService {
    RefreshToken createToken(User user, long durationMinutes);
    boolean isValid(String token);
    User getUserFromToken(String token);
    void deleteToken(String token);
}
