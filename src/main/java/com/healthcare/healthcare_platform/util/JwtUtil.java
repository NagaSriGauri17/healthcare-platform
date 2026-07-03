package com.healthcare.healthcare_platform.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET = "healthcare_secret_key_must_be_32_characters_long";
    private static final long EXPIRATION = 86400000;

    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // Original method — kept for patient login (no hospital context needed)
    public String generateToken(String identifier) {
        return generateToken(identifier, null, "PATIENT");
    }

    // New — for staff logins, embeds hospitalId + role
    public String generateToken(String identifier, Long hospitalId, String role) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(identifier)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION));
        if (hospitalId != null) {
            builder.claim("hospitalId", hospitalId);
        }
        return builder.signWith(getKey(), SignatureAlgorithm.HS256).compact();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Long extractHospitalId(String token) {
        Object val = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("hospitalId");
        return val != null ? Long.valueOf(val.toString()) : null;
    }

    public String extractRole(String token) {
        return (String) Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role");
    }

    public boolean validateToken(String token) {
        try {
            extractEmail(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}