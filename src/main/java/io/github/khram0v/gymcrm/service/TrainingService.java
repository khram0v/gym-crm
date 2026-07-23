package io.github.khram0v.gymcrm.service;

import io.github.khram0v.gymcrm.model.Training;

import java.time.LocalDate;
import java.util.List;

public interface TrainingService {

    Training addTraining(String trainerUsername,
                         String traineeUsername,
                         String trainingName,
                         LocalDate trainingDate,
                         Integer duration);

    List<Training> getTraineeTrainings(String traineeUsername,
                                       LocalDate from,
                                       LocalDate to,
                                       String trainerFirstName,
                                       String trainerLastName,
                                       String trainingTypeName);

    List<Training> getTrainerTrainings(String trainerUsername,
                                       LocalDate from,
                                       LocalDate to,
                                       String traineeFirstName,
                                       String traineeLastName);
}
