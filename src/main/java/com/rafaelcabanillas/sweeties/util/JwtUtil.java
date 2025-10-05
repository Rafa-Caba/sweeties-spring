package com.rafaelcabanillas.sweeties.util;

import com.rafaelcabanillas.sweeties.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 15 * 60 * 1000); // 15 minutes
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", user.getRole()) // or roles array
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }
}
