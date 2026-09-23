package io.github.khram0v.gymcrm.integration.support;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

public final class ServiceJwtIssuer {

    private static final String SECRET = "local-only-dev-shared-secret-key-not-for-prod-use-1234567890";

    private ServiceJwtIssuer() {
    }

    public static String serviceToken(String subject) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .claim("type", "service")
                .issuedAt(Date.from(Instant.now().minusSeconds(5)))
                .expiration(Date.from(Instant.now().plusSeconds(300)))
                .signWith(key)
                .compact();
    }
}
