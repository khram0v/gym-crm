package io.github.khram0v.gymcrm.mapper;

import io.github.khram0v.gymcrm.dto.response.TraineeTrainingResponse;
import io.github.khram0v.gymcrm.dto.response.TrainerTrainingResponse;
import io.github.khram0v.gymcrm.model.Training;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TrainingMapper {

    @Mapping(source = "trainingType.trainingTypeName", target = "trainingType")
    @Mapping(source = "trainer.firstName", target = "trainerFirstName")
    @Mapping(source = "trainer.lastName", target = "trainerLastName")
    TraineeTrainingResponse toTraineeTrainingResponse(Training training);

    List<TraineeTrainingResponse> toTraineeTrainingResponses(List<Training> trainings);

    @Mapping(source = "trainingType.trainingTypeName", target = "trainingType")
    @Mapping(source = "trainee.firstName", target = "traineeFirstName")
    @Mapping(source = "trainee.lastName", target = "traineeLastName")
    TrainerTrainingResponse toTrainerTrainingResponse(Training training);

    List<TrainerTrainingResponse> toTrainerTrainingResponses(List<Training> trainings);
}
