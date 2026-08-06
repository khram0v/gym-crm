package io.github.khram0v.gymcrm.security.bruteforce;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import io.github.khram0v.gymcrm.exception.AccountLockedException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(5);
    private static final Duration ENTRY_TTL = Duration.ofMinutes(10);

    private final Cache<String, AttemptRecord> attempts = Caffeine.newBuilder()
            .expireAfterWrite(ENTRY_TTL)
            .scheduler(Scheduler.systemScheduler())
            .build();

    public void checkNotBlocked(String username) {
        AttemptRecord attemptRecord = attempts.getIfPresent(username);
        if (attemptRecord == null) {
            return;
        }

        Instant lockedUntil = attemptRecord.lockedUntil();
        if (lockedUntil == null) {
            return;
        }

        if (Instant.now().isBefore(lockedUntil)) {
            throw new AccountLockedException(
                    "Too many failed login attempts for '" +  username + "'. Locked until " + lockedUntil);
        }

        attempts.invalidate(username);
    }

    public void loginFailed(String username) {
        attempts.get(username, key -> new AttemptRecord()).recordFailure();
    }

    public void loginSucceeded(String username) {
        attempts.invalidate(username);
    }

    private static final class AttemptRecord {
        private final AtomicInteger failedAttempts = new AtomicInteger();
        private volatile Instant lockedUntil;

        void recordFailure() {
            if (failedAttempts.incrementAndGet() >= MAX_ATTEMPTS) {
                lockedUntil = Instant.now().plus(LOCK_DURATION);
            }
        }

        Instant lockedUntil() {
            return lockedUntil;
        }
    }
}
