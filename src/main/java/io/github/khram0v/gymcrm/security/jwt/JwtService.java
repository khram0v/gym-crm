package io.github.khram0v.gymcrm.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.time.Instant;

@Component
public class JwtService {

    private static final String ROLE_CLAIM = "role";
    private static final String TYPE_CLAIM = "type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final SecretKey key;
    @Getter private final long expirationMs;
    @Getter private final long refreshExpirationMs;

    public JwtService(JwtProperties properties) {
        byte[] secretBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes long");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expirationMs = properties.expirationMs();
        this.refreshExpirationMs = properties.refreshExpirationMs();
    }

    public String generateToken(String username, String role) {
        return buildToken(username, role, ACCESS_TOKEN_TYPE, expirationMs);
    }

    public String generateRefreshToken(String username, String role) {
        return buildToken(username, role, REFRESH_TOKEN_TYPE, refreshExpirationMs);
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get(ROLE_CLAIM, String.class);
    }

    public Instant extractExpiration(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    public boolean isValid(String token, String expectedUsername) {
        return isValidOfType(token, expectedUsername, ACCESS_TOKEN_TYPE);
    }

    public boolean isValidRefreshToken(String token, String expectedUsername) {
        return isValidOfType(token, expectedUsername, REFRESH_TOKEN_TYPE);
    }

    private boolean isValidOfType(String token, String expectedUsername, String expectedType) {
        try {
            Claims claims = parseClaims(token);
            return claims.getSubject().equals(expectedUsername)
                    && claims.getExpiration().after(new Date())
                    && expectedType.equals(claims.get(TYPE_CLAIM, String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private String buildToken(String username, String role, String type, long ttlMs) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim(ROLE_CLAIM, role)
                .claim(TYPE_CLAIM, type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(ttlMs)))
                .signWith(key)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
