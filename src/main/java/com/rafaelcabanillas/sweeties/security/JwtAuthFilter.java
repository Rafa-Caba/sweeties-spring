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
import java.io.IOException;
import java.security.Key;
import java.util.*;

@Component
public class JwtAuthFilter extends GenericFilter {

    // Use a strong secret, not hardcoded in prod!
    private static final String SECRET = "very-strong-secret-key-for-jwt-signing-must-be-256-bit";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            try {
                Claims claims = Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(jwt).getBody();
                String username = claims.getSubject();
                List<GrantedAuthority> authorities = new ArrayList<>();
                Object roles = claims.get("roles");
                if (roles instanceof String roleStr) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + roleStr));
                } else if (roles instanceof Collection<?>) {
                    for (Object role : (Collection<?>) roles) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                }
                Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException e) {
                // invalid token, ignore (will result in 401 for protected routes)
            }
        }
        chain.doFilter(request, response);
    }
}
