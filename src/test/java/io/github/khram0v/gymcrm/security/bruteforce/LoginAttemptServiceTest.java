package io.github.khram0v.gymcrm.security.bruteforce;

import io.github.khram0v.gymcrm.exception.AccountLockedException;
import io.github.khram0v.gymcrm.testsupport.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginAttemptServiceTest {

    private MutableClock clock;
    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2024-01-01T00:00:00Z"));
        loginAttemptService = new LoginAttemptService(clock);
    }

    @Test
    void checkNotBlocked_whenNoAttempts_doesNotThrow() {
        assertThatCode(() -> loginAttemptService.checkNotBlocked("John.Doe"))
                .doesNotThrowAnyException();
    }

    @Test
    void checkNotBlocked_whenLessThanThreeFailures_doesNotThrow() {
        loginAttemptService.loginFailed("John.Doe");
        loginAttemptService.loginFailed("John.Doe");

        assertThatCode(() -> loginAttemptService.checkNotBlocked("John.Doe"))
                .doesNotThrowAnyException();
    }

    @Test
    void checkNotBlocked_whenThreeFailures_throwsAccountLocked() {
        loginAttemptService.loginFailed("John.Doe");
        loginAttemptService.loginFailed("John.Doe");
        loginAttemptService.loginFailed("John.Doe");

        assertThatThrownBy(() -> loginAttemptService.checkNotBlocked("John.Doe"))
                .isInstanceOf(AccountLockedException.class)
                .hasMessageContaining("John.Doe");
    }

    @Test
    void checkNotBlocked_afterLockExpires_doesNotThrow_andResetsCounter() {
        loginAttemptService.loginFailed("John.Doe");
        loginAttemptService.loginFailed("John.Doe");
        loginAttemptService.loginFailed("John.Doe");

        clock.advanceBy(Duration.ofMinutes(5).plusSeconds(1));

        assertThatCode(() -> loginAttemptService.checkNotBlocked("John.Doe"))
                .doesNotThrowAnyException();

        loginAttemptService.loginFailed("John.Doe");
        assertThatCode(() -> loginAttemptService.checkNotBlocked("John.Doe"))
                .doesNotThrowAnyException();
    }

    @Test
    void loginSucceeded_clearsFailureCount() {
        loginAttemptService.loginFailed("John.Doe");
        loginAttemptService.loginFailed("John.Doe");

        loginAttemptService.loginSucceeded("John.Doe");
        loginAttemptService.loginFailed("John.Doe");
        loginAttemptService.loginFailed("John.Doe");

        assertThatCode(() -> loginAttemptService.checkNotBlocked("John.Doe"))
                .doesNotThrowAnyException();
    }

    @Test
    void loginFailed_tracksUsernamesIndependently() {
        loginAttemptService.loginFailed("John.Doe");
        loginAttemptService.loginFailed("John.Doe");
        loginAttemptService.loginFailed("John.Doe");

        assertThatCode(() -> loginAttemptService.checkNotBlocked("Jane.Smith"))
                .doesNotThrowAnyException();
    }
}
