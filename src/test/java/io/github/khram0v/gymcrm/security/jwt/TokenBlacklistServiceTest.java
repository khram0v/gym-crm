package io.github.khram0v.gymcrm.security.jwt;

import io.github.khram0v.gymcrm.testsupport.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBlacklistServiceTest {

    private TokenBlacklistService tokenBlacklistService;
    private MutableClock clock;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2024-01-01T00:00:00Z"));
        tokenBlacklistService = new TokenBlacklistService(clock);
    }

    @Test
    void isBlacklisted_whenTokenNeverBlacklisted_returnsFalse() {
        assertThat(tokenBlacklistService.isBlacklisted("some-token")).isFalse();
    }

    @Test
    void isBlacklisted_afterBlacklisting_returnsTrue() {
        Instant expiresAt = clock.instant().plus(Duration.ofMinutes(30));

        tokenBlacklistService.blacklist("some-token", expiresAt);

        assertThat(tokenBlacklistService.isBlacklisted("some-token")).isTrue();
    }

    @Test
    void isBlacklisted_doesNotAffectOtherTokens() {
        tokenBlacklistService.blacklist("token-a", clock.instant().plus(Duration.ofMinutes(30)));

        assertThat(tokenBlacklistService.isBlacklisted("token-b")).isFalse();
    }
}
