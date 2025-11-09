package com.rafaelcabanillas.sweeties.util;

import com.rafaelcabanillas.sweeties.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.*;

@Component
public class JwtUtil {

    // move to env/props in prod; length >= 32 bytes for HS256
    private static final String SECRET = "very-strong-secret-key-for-jwt-signing-must-be-256-bit";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    // e.g., 15 min access token
    private static final long EXP_MINUTES = 10080;

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        // your filter expects "roles"
        claims.put("roles", List.of(user.getRole().name()));

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(EXP_MINUTES * 60);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())   // or user.getEmail() or user.getId().toString()
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }
}