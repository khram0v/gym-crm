package io.github.khram0v.gymcrm.health;

import io.github.khram0v.gymcrm.dto.response.TrainingTypeResponse;
import io.github.khram0v.gymcrm.service.TrainingTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferenceDataHealthIndicatorTest {

    @Mock
    private TrainingTypeService trainingTypeService;

    @InjectMocks
    private ReferenceDataHealthIndicator indicator;

    @Test
    void health_returnsUp_whenTrainingTypesExist() {
        List<TrainingTypeResponse> types = List.of(
                new TrainingTypeResponse(1L, "Fitness"),
                new TrainingTypeResponse(2L, "Yoga"));
        when(trainingTypeService.getAll()).thenReturn(types);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("trainingTypesCount", 2);
    }

    @Test
    void health_returnsDown_whenTrainingTypesEmpty() {
        when(trainingTypeService.getAll()).thenReturn(List.of());

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
                .containsEntry("reason", "training_type reference data is empty");
    }

    @Test
    void health_returnsDown_whenServiceThrows() {
        when(trainingTypeService.getAll()).thenThrow(new RuntimeException("DB unreachable"));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails().get("error").toString()).contains("DB unreachable");
    }
}
