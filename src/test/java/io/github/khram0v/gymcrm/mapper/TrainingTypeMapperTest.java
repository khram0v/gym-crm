package io.github.khram0v.gymcrm.mapper;

import io.github.khram0v.gymcrm.dto.response.TrainingTypeResponse;
import io.github.khram0v.gymcrm.model.TrainingType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrainingTypeMapperTest {

    private final TrainingTypeMapper mapper = new TrainingTypeMapperImpl();

    @Test
    void toResponse_mapsIdAndRenamesNameField() {
        TrainingType type = new TrainingType("Fitness");
        type.setId(1L);

        TrainingTypeResponse response = mapper.toResponse(type);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Fitness");
    }

    @Test
    void toResponse_whenNull_returnsNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    void toResponseList_mapsAll() {
        TrainingType fitness = new TrainingType("Fitness");
        TrainingType yoga = new TrainingType("Yoga");

        List<TrainingTypeResponse> result = mapper.toResponseList(List.of(fitness, yoga));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TrainingTypeResponse::name)
                .containsExactly("Fitness", "Yoga");
    }
}
