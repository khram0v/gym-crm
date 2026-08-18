package io.github.khram0v.gymcrm.mapper;

import io.github.khram0v.gymcrm.dto.response.TraineeTrainingResponse;
import io.github.khram0v.gymcrm.dto.response.TrainerTrainingResponse;
import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.model.Training;
import io.github.khram0v.gymcrm.model.TrainingType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrainingMapperTest {

    private final TrainingMapper mapper = new TrainingMapperImpl();

    private Training sampleTraining() {
        TrainingType fitness = new TrainingType("Fitness");
        Trainer trainer = new Trainer("Jane", "Smith", fitness);
        trainer.setUsername("Jane.Smith");
        Trainee trainee = new Trainee("John", "Doe", null, null);
        trainee.setUsername("John.Doe");
        Training training = new Training(trainee, trainer, "Morning Fitness",
                fitness, LocalDate.of(2024, Month.JUNE, 1), 60);
        training.setId(42L);
        return training;
    }

    @Test
    void toTraineeTrainingResponse_flattensTrainerNameAndType() {
        TraineeTrainingResponse response = mapper.toTraineeTrainingResponse(sampleTraining());

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.trainingName()).isEqualTo("Morning Fitness");
        assertThat(response.trainingDate()).isEqualTo(LocalDate.of(2024, Month.JUNE, 1));
        assertThat(response.trainingType()).isEqualTo("Fitness");
        assertThat(response.trainingDuration()).isEqualTo(60);
        assertThat(response.trainerFirstName()).isEqualTo("Jane");
        assertThat(response.trainerLastName()).isEqualTo("Smith");
    }

    @Test
    void toTrainerTrainingResponse_flattensTraineeNameAndType() {
        TrainerTrainingResponse response = mapper.toTrainerTrainingResponse(sampleTraining());

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.trainingName()).isEqualTo("Morning Fitness");
        assertThat(response.trainingType()).isEqualTo("Fitness");
        assertThat(response.trainingDuration()).isEqualTo(60);
        assertThat(response.traineeFirstName()).isEqualTo("John");
        assertThat(response.traineeLastName()).isEqualTo("Doe");
    }

    @Test
    void toTraineeTrainingResponses_mapsList() {
        List<TraineeTrainingResponse> result =
                mapper.toTraineeTrainingResponses(List.of(sampleTraining(), sampleTraining()));

        assertThat(result).hasSize(2);
    }
}
