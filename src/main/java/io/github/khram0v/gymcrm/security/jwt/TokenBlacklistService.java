package io.github.khram0v.gymcrm.security.jwt;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
public class TokenBlacklistService {

    private final Cache<String, Instant> blacklistedTokens;
    private final Clock clock;

    @Autowired
    public TokenBlacklistService() {
        this(Clock.systemUTC());
    }

    TokenBlacklistService(Clock clock) {
        this.clock = clock;
        this.blacklistedTokens = Caffeine.newBuilder()
                .expireAfter(new Expiry<String, Instant>() {
                    @Override
                    public long expireAfterCreate(String token, Instant expiresAt, long currentTime) {
                        return remainingNanos(expiresAt);
                    }

                    @Override
                    public long expireAfterUpdate(String token, Instant expiresAt,
                                                  long currentTime, long currentDuration) {
                        return remainingNanos(expiresAt);
                    }

                    @Override
                    public long expireAfterRead(String token, Instant expiresAt,
                                                long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                })
                .scheduler(Scheduler.systemScheduler())
                .build();
    }

    public void blacklist(String token, Instant expiresAt) {
        blacklistedTokens.put(token, expiresAt);
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokens.getIfPresent(token) != null;
    }

    private long remainingNanos(Instant expiresAt) {
        Duration remaining = Duration.between(clock.instant(), expiresAt);
        return remaining.isNegative() ? 0 : remaining.toNanos();
    }
}
