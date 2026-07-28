package io.github.khram0v.gymcrm.mapper;

import io.github.khram0v.gymcrm.dto.response.TraineeSummary;
import io.github.khram0v.gymcrm.dto.response.TrainerSummary;
import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.model.TrainingType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SummaryMapperTest {

    private final TrainingTypeMapper typeMapper = new TrainingTypeMapperImpl();
    private final SummaryMapper mapper = new SummaryMapperImpl(typeMapper);

    @Test
    void toTrainerSummary_mapsFieldsAndSpecialization() {
        TrainingType fitness = new TrainingType("Fitness");
        fitness.setId(5L);
        Trainer trainer = new Trainer("Jane", "Smith", fitness);
        trainer.setUsername("Jane.Smith");

        TrainerSummary summary = mapper.toTrainerSummary(trainer);

        assertThat(summary.username()).isEqualTo("Jane.Smith");
        assertThat(summary.firstName()).isEqualTo("Jane");
        assertThat(summary.lastName()).isEqualTo("Smith");
        assertThat(summary.specialization().id()).isEqualTo(5L);
        assertThat(summary.specialization().name()).isEqualTo("Fitness");
    }

    @Test
    void toTraineeSummary_mapsFields() {
        Trainee trainee = new Trainee("John", "Doe", null, null);
        trainee.setUsername("John.Doe");

        TraineeSummary summary = mapper.toTraineeSummary(trainee);

        assertThat(summary.username()).isEqualTo("John.Doe");
        assertThat(summary.firstName()).isEqualTo("John");
        assertThat(summary.lastName()).isEqualTo("Doe");
    }

    @Test
    void trainerSetToSortedSummaries_sortsByUsername() {
        Trainer z = new Trainer("Zoe", "Zed", new TrainingType("Yoga"));
        z.setUsername("Zoe.Zed");
        Trainer a = new Trainer("Ann", "Lee", new TrainingType("Boxing"));
        a.setUsername("Ann.Lee");
        Trainer m = new Trainer("Mia", "Fox", new TrainingType("Pilates"));
        m.setUsername("Mia.Fox");

        List<TrainerSummary> result = mapper.trainerSetToSortedSummaries(Set.of(z, a, m));

        assertThat(result).extracting(TrainerSummary::username)
                .containsExactly("Ann.Lee", "Mia.Fox", "Zoe.Zed");
    }

    @Test
    void traineeSetToSortedSummaries_sortsByUsername() {
        Trainee c = new Trainee("Carl", "Cox", null, null);
        c.setUsername("Carl.Cox");
        Trainee b = new Trainee("Bob", "Ray", null, null);
        b.setUsername("Bob.Ray");

        List<TraineeSummary> result = mapper.traineeSetToSortedSummaries(Set.of(c, b));

        assertThat(result).extracting(TraineeSummary::username)
                .containsExactly("Bob.Ray", "Carl.Cox");
    }

    @Test
    void trainerSetToSortedSummaries_whenNull_returnsEmptyList() {
        assertThat(mapper.trainerSetToSortedSummaries(null)).isEmpty();
    }

    @Test
    void traineeSetToSortedSummaries_whenNull_returnsEmptyList() {
        assertThat(mapper.traineeSetToSortedSummaries(null)).isEmpty();
    }
}
