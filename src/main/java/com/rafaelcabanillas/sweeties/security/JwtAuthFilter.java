package com.rafaelcabanillas.sweeties.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.*;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String SECRET = "very-strong-secret-key-for-jwt-signing-must-be-256-bit";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/public/")
//                || path.equals("/api/orders")
                || path.equals("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = req.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(KEY)
                        .build()
                        .parseClaimsJws(jwt)
                        .getBody();

                String username = claims.getSubject();
                List<GrantedAuthority> authorities = new ArrayList<>();
                Object roles = claims.get("roles");

                if (roles instanceof String roleStr) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + roleStr));
                } else if (roles instanceof Collection<?> coll) {
                    for (Object role : coll) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                }

                Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException ignored) {
                // invalid token → fall through to 401 by entry point
            }
        }
        chain.doFilter(req, res);
    }
}

