package io.github.khram0v.gymcrm.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-32-bytes-long!!";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new JwtProperties(SECRET, 60_000));
    }

    @Test
    void constructor_whenSecretTooShort_throwsIllegalStateException() {
        assertThatThrownBy(() -> new JwtService(new JwtProperties("short", 60_000)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    @Test
    void generateToken_thenExtractUsername_roundTrips() {
        String token = jwtService.generateToken("John.Doe", "TRAINEE");

        assertThat(jwtService.extractUsername(token)).isEqualTo("John.Doe");
    }

    @Test
    void generateToken_thenExtractRole_roundTrips() {
        String token = jwtService.generateToken("John.Doe", "TRAINEE");

        assertThat(jwtService.extractRole(token)).isEqualTo("TRAINEE");
    }

    @Test
    void generateToken_expirationIsInTheFuture() {
        String token = jwtService.generateToken("John.Doe", "TRAINEE");

        assertThat(jwtService.extractExpiration(token)).isAfter(Instant.now());
    }

    @Test
    void isValid_whenUsernameMatchesAndNotExpired_returnsTrue() {
        String token = jwtService.generateToken("John.Doe", "TRAINEE");

        assertThat(jwtService.isValid(token, "John.Doe")).isTrue();
    }

    @Test
    void isValid_whenUsernameDoesNotMatch_returnsFalse() {
        String token = jwtService.generateToken("John.Doe", "TRAINEE");

        assertThat(jwtService.isValid(token, "Someone.Else")).isFalse();
    }

    @Test
    void isValid_whenTokenExpired_returnsFalse() throws InterruptedException {
        JwtService shortLivedJwtService = new JwtService(new JwtProperties(SECRET, 1));
        String token = shortLivedJwtService.generateToken("John.Doe", "TRAINEE");

        Thread.sleep(20);

        assertThat(shortLivedJwtService.isValid(token, "John.Doe")).isFalse();
    }

    @Test
    void isValid_whenTokenSignedWithDifferentSecret_returnsFalse() {
        JwtService otherJwtService = new JwtService(
                new JwtProperties("a-completely-different-secret-key-of-32-bytes!!", 60_000));
        String token = otherJwtService.generateToken("John.Doe", "TRAINEE");

        assertThat(jwtService.isValid(token, "John.Doe")).isFalse();
    }

    @Test
    void isValid_whenTokenMalformed_returnsFalse() {
        assertThat(jwtService.isValid("not-a-jwt", "John.Doe")).isFalse();
    }

    @Test
    void getExpirationMs_returnsConfiguredValue() {
        assertThat(jwtService.getExpirationMs()).isEqualTo(60_000);
    }
}
