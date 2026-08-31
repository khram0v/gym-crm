package io.github.khram0v.gymcrm.client.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceTokenProviderTest {

    private static final String SECRET = "test-service-secret-key-must-be-at-least-32-bytes!!";

    @Test
    void constructor_whenSecretTooShort_throwsIllegalStateException() {
        assertThatThrownBy(() ->
                new ServiceTokenProvider(new ServiceJwtProperties("short", "gym-crm-service", 60_000)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    @Test
    void generateToken_setsSubjectAndServiceTypeClaim() {
        ServiceTokenProvider provider = new ServiceTokenProvider(
                new ServiceJwtProperties(SECRET, "gym-crm-service", 60_000));

        Claims claims = parseClaims(provider.generateToken());

        assertThat(claims.getSubject()).isEqualTo("gym-crm-service");
        assertThat(claims.get("type", String.class)).isEqualTo("service");
    }

    @Test
    void generateToken_expirationIsInTheFuture() {
        ServiceTokenProvider provider = new ServiceTokenProvider(
                new ServiceJwtProperties(SECRET, "gym-crm-service", 60_000));

        Claims claims = parseClaims(provider.generateToken());

        assertThat(claims.getExpiration().toInstant()).isAfter(Instant.now());
    }

    @Test
    void generateToken_usesConfiguredSubject() {
        ServiceTokenProvider provider = new ServiceTokenProvider(
                new ServiceJwtProperties(SECRET, "custom-subject", 60_000));

        Claims claims = parseClaims(provider.generateToken());

        assertThat(claims.getSubject()).isEqualTo("custom-subject");
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
