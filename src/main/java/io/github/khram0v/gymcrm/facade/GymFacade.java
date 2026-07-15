package io.github.khram0v.gymcrm.facade;

import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.model.Training;
import io.github.khram0v.gymcrm.model.TrainingType;
import io.github.khram0v.gymcrm.service.TraineeService;
import io.github.khram0v.gymcrm.service.TrainerService;
import io.github.khram0v.gymcrm.service.TrainingService;
import io.github.khram0v.gymcrm.service.TrainingTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;

    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService,
                     TrainingTypeService trainingTypeService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
    }

    // ~~~~~ Trainee operations ~~~~~

    public Trainee createTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        return traineeService.create(firstName, lastName, dateOfBirth, address);
    }

    public Trainee getTrainee(String username, String password) {
        return traineeService.getByUsername(username, password);
    }

    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        traineeService.changePassword(username, oldPassword, newPassword);
    }

    public Trainee updateTrainee(String username, String password,
                                 String firstName, String lastName,
                                 LocalDate dateOfBirth, String address) {
        return traineeService.updateProfile(username, password, firstName, lastName, dateOfBirth, address);
    }

    public void setTraineeActive(String username, String password, boolean active) {
        traineeService.setActiveStatus(username, password, active);
    }

    public void deleteTrainee(String username, String password) {
        traineeService.deleteByUsername(username, password);
    }

    public List<Trainer> getUnassignedTrainers(String username, String password) {
        return traineeService.getUnassignedTrainers(username, password);
    }

    public List<Trainer> updateTraineeTrainers(String username, String password, List<String> trainerUsernames) {
        return traineeService.updateTrainers(username, password, trainerUsernames);
    }

    // ~~~~~ Trainer operations ~~~~~

    public Trainer createTrainer(String firstName, String lastName, Long specializationId) {
        return trainerService.create(firstName, lastName, specializationId);
    }

    public Trainer getTrainer(String username, String password) {
        return trainerService.getByUsername(username, password);
    }

    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
        trainerService.changePassword(username, oldPassword, newPassword);
    }

    public Trainer updateTrainer(String username, String password,
                                 String firstName, String lastName,
                                 Long specializationId) {
        return trainerService.updateProfile(username, password, firstName, lastName, specializationId);
    }

    public void setTrainerActive(String username, String password, boolean active) {
        trainerService.setActiveStatus(username, password, active);
    }

    // ~~~~~ Training operations ~~~~~

    public Training addTraining(String trainerUsername, String password,
                                String traineeUsername,
                                String trainingName,
                                Long trainingTypeId,
                                LocalDate trainingDate,
                                Integer duration) {
        return trainingService.addTraining(trainerUsername, password, traineeUsername,
                trainingName, trainingTypeId, trainingDate, duration);
    }

    public List<Training> getTraineeTrainings(String traineeUsername, String password,
                                              LocalDate fromDate, LocalDate toDate,
                                              String trainerFirstName, String trainerLastName,
                                              String trainingTypeName) {
        return trainingService.getTraineeTrainings(traineeUsername, password, fromDate, toDate,
                trainerFirstName, trainerLastName, trainingTypeName);
    }

    public List<Training> getTrainerTrainings(String trainerUsername, String password,
                                              LocalDate fromDate, LocalDate toDate,
                                              String traineeFirstName, String traineeLastName) {
        return trainingService.getTrainerTrainings(trainerUsername, password, fromDate, toDate,
                traineeFirstName, traineeLastName);
    }

    // ~~~~~ Training types ~~~~~

    public List<TrainingType> getAllTrainingTypes() {
        return trainingTypeService.getAll();
    }
}
