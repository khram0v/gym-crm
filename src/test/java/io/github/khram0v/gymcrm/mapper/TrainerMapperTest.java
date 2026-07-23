package io.github.khram0v.gymcrm.mapper;

import io.github.khram0v.gymcrm.dto.response.RegistrationResponse;
import io.github.khram0v.gymcrm.dto.response.TrainerProfileResponse;
import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.model.TrainingType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TrainerMapperTest {

    private final TrainingTypeMapper typeMapper = new TrainingTypeMapperImpl();
    private final SummaryMapper summaryMapper = new SummaryMapperImpl(typeMapper);
    private final TrainerMapper mapper = new TrainerMapperImpl(summaryMapper, typeMapper);

    @Test
    void toRegistrationResponse_mapsUsernameAndPassword() {
        Trainer trainer = new Trainer("Jane", "Smith", new TrainingType("Fitness"));
        trainer.setUsername("Jane.Smith");
        trainer.setPassword("pass456");

        RegistrationResponse response = mapper.toRegistrationResponse(trainer);

        assertThat(response.username()).isEqualTo("Jane.Smith");
        assertThat(response.password()).isEqualTo("pass456");
    }

    @Test
    void toProfileResponse_mapsFieldsSpecialization_andSortsTrainees() {
        TrainingType fitness = new TrainingType("Fitness");
        fitness.setId(3L);
        Trainer trainer = new Trainer("Jane", "Smith", fitness);
        trainer.setUsername("Jane.Smith");
        trainer.setActive(true);

        Trainee carl = new Trainee("Carl", "Cox", null, null);
        carl.setUsername("Carl.Cox");
        Trainee bob = new Trainee("Bob", "Ray", null, null);
        bob.setUsername("Bob.Ray");
        trainer.setTrainees(Set.of(carl, bob));

        TrainerProfileResponse response = mapper.toProfileResponse(trainer);

        assertThat(response.username()).isEqualTo("Jane.Smith");
        assertThat(response.firstName()).isEqualTo("Jane");
        assertThat(response.lastName()).isEqualTo("Smith");
        assertThat(response.active()).isTrue();
        assertThat(response.specialization().id()).isEqualTo(3L);
        assertThat(response.specialization().name()).isEqualTo("Fitness");
        assertThat(response.trainees()).extracting(t -> t.username())
                .containsExactly("Bob.Ray", "Carl.Cox");
    }
}
