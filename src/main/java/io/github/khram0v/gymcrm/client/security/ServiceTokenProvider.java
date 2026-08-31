package io.github.khram0v.gymcrm.client.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class ServiceTokenProvider {

    private static final String TYPE_CLAIM = "type";
    private static final String SERVICE_TOKEN_TYPE = "service";

    private final SecretKey key;
    private final String subject;
    private final long expirationMs;

    public ServiceTokenProvider(ServiceJwtProperties properties) {
        byte[] secretBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("Service JWT secret must be at least 32 bytes long");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.subject = properties.subject();
        this.expirationMs = properties.expirationMs();
    }

    public String generateToken() {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claim(TYPE_CLAIM, SERVICE_TOKEN_TYPE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key)
                .compact();
    }
}
