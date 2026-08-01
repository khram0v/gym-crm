package io.github.khram0v.gymcrm.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final long SLOW_THRESHOLD_MS = 500;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Health health() {
        long start = System.currentTimeMillis();
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            long elapsedMs = System.currentTimeMillis() - start;
            log.debug("Database ping succeeded in {} ms", elapsedMs);

            Health.Builder builder = elapsedMs > SLOW_THRESHOLD_MS
                    ? Health.status("DEGRADED").withDetail("reason", "slow response")
                    : Health.up();

            return builder.withDetail("responseTimeMs", elapsedMs).build();
        } catch (DataAccessException ex) {
            log.error("Database health check failed", ex);
            return Health.down(ex).build();
        }
    }
}
