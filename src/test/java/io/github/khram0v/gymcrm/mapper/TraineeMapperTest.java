package io.github.khram0v.gymcrm.mapper;

import io.github.khram0v.gymcrm.dto.response.TraineeProfileResponse;
import io.github.khram0v.gymcrm.dto.response.TrainerSummary;
import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.model.TrainingType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TraineeMapperTest {

    private final TrainingTypeMapper typeMapper = new TrainingTypeMapperImpl();
    private final SummaryMapper summaryMapper = new SummaryMapperImpl(typeMapper);
    private final TraineeMapper mapper = new TraineeMapperImpl(summaryMapper);

    @Test
    void toProfileResponse_mapsAllFields_andSortsTrainers() {
        Trainee trainee = new Trainee("John", "Doe",
                LocalDate.of(1995, Month.MAY, 20), "123 Main St");
        trainee.setUsername("John.Doe");
        trainee.setActive(true);

        Trainer z = new Trainer("Zoe", "Zed", new TrainingType("Yoga"));
        z.setUsername("Zoe.Zed");
        Trainer a = new Trainer("Ann", "Lee", new TrainingType("Boxing"));
        a.setUsername("Ann.Lee");
        trainee.setTrainers(Set.of(z, a));

        TraineeProfileResponse response = mapper.toProfileResponse(trainee);

        assertThat(response.username()).isEqualTo("John.Doe");
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.dateOfBirth()).isEqualTo(LocalDate.of(1995, Month.MAY, 20));
        assertThat(response.address()).isEqualTo("123 Main St");
        assertThat(response.active()).isTrue();
        assertThat(response.trainers()).extracting(TrainerSummary::username)
                .containsExactly("Ann.Lee", "Zoe.Zed");
    }

    @Test
    void toProfileResponse_whenNoTrainers_returnsEmptyList() {
        Trainee trainee = new Trainee("John", "Doe", null, null);
        trainee.setUsername("John.Doe");
        trainee.setActive(false);

        TraineeProfileResponse response = mapper.toProfileResponse(trainee);

        assertThat(response.trainers()).isEmpty();
    }
}
