package io.github.khram0v.gymcrm.service;

import io.github.khram0v.gymcrm.dto.response.TraineeTrainingResponse;
import io.github.khram0v.gymcrm.dto.response.TrainerTrainingResponse;

import java.time.LocalDate;
import java.util.List;

public interface TrainingService {

    void addTraining(String trainerUsername,
                         String traineeUsername,
                         String trainingName,
                         LocalDate trainingDate,
                         Integer duration);

    void deleteTraining(Long trainingId);

    List<TraineeTrainingResponse> getTraineeTrainings(String traineeUsername,
                                                      LocalDate from,
                                                      LocalDate to,
                                                      String trainerFirstName,
                                                      String trainerLastName,
                                                      String trainingTypeName);

    List<TrainerTrainingResponse> getTrainerTrainings(String trainerUsername,
                                                      LocalDate from,
                                                      LocalDate to,
                                                      String traineeFirstName,
                                                      String traineeLastName);
}
