package io.github.khram0v.gymcrm.health;

import io.github.khram0v.gymcrm.service.TrainingTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReferenceDataHealthIndicator implements HealthIndicator {

    private final TrainingTypeService trainingTypeService;

    @Override
    public Health health() {
        try {
            var trainingTypes = trainingTypeService.getAll();
            if (trainingTypes.isEmpty()) {
                log.warn("Reference data health check failed: no training types found");
                return Health.down()
                        .withDetail("reason", "training_type reference data is empty")
                        .build();
            }
            return Health.up().withDetail("trainingTypesCount", trainingTypes.size()).build();
        } catch (Exception ex) {
            log.error("Reference data health check failed", ex);
            return Health.down(ex).build();
        }
    }
}
