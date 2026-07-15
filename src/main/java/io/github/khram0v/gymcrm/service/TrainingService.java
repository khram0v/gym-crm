package io.github.khram0v.gymcrm.service;

import io.github.khram0v.gymcrm.repository.TraineeRepository;
import io.github.khram0v.gymcrm.repository.TrainerRepository;
import io.github.khram0v.gymcrm.repository.TrainingRepository;
import io.github.khram0v.gymcrm.repository.TrainingTypeRepository;
import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.model.Training;
import io.github.khram0v.gymcrm.model.TrainingType;
import io.github.khram0v.gymcrm.repository.specification.TrainingSpecification;
import io.github.khram0v.gymcrm.validation.EntityValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final AuthenticationService authenticationService;
    private final EntityValidator validator;

    public TrainingService(TrainingRepository trainingRepository,
                           TrainerRepository trainerRepository,
                           TraineeRepository traineeRepository,
                           TrainingTypeRepository trainingTypeRepository,
                           AuthenticationService authenticationService,
                           EntityValidator validator) {
        this.trainingRepository = trainingRepository;
        this.trainerRepository = trainerRepository;
        this.traineeRepository = traineeRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.authenticationService = authenticationService;
        this.validator = validator;
    }

    @Transactional
    public Training addTraining(String trainerUsername, String password,
                                String traineeUsername,
                                String trainingName,
                                Long trainingTypeId,
                                LocalDate trainingDate,
                                Integer duration) {
        authenticationService.authenticateTrainer(trainerUsername, password);

        Trainer trainer = trainerRepository.findByUsername(trainerUsername)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + trainerUsername));
        Trainee trainee = traineeRepository.findByUsername(traineeUsername)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + traineeUsername));
        TrainingType type = trainingTypeRepository.findById(trainingTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Training type not found: id=" + trainingTypeId));

        Training training = new Training(trainee, trainer, trainingName, type, trainingDate, duration);
        validator.validate(training);
        Training saved = trainingRepository.save(training);
        log.info("Added training '{}' for trainee '{}' with trainer '{}'",
                trainingName, traineeUsername, trainerUsername);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(String traineeUsername, String password,
                                              LocalDate fromDate, LocalDate toDate,
                                              String trainerFirstName, String trainerLastName,
                                              String trainingTypeName) {
        authenticationService.authenticateTrainee(traineeUsername, password);
        Specification<Training> spec = Specification.allOf(
                TrainingSpecification.hasTraineeUsername(traineeUsername),
                TrainingSpecification.dateFrom(fromDate),
                TrainingSpecification.dateTo(toDate),
                TrainingSpecification.trainerFirstName(trainerFirstName),
                TrainingSpecification.trainerLastName(trainerLastName),
                TrainingSpecification.trainingTypeName(trainingTypeName)
        );
        return trainingRepository.findAll(spec);
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(String trainerUsername, String password,
                                              LocalDate fromDate, LocalDate toDate,
                                              String traineeFirstName, String traineeLastName) {
        authenticationService.authenticateTrainer(trainerUsername, password);
        Specification<Training> spec = Specification.allOf(
                TrainingSpecification.hasTrainerUsername(trainerUsername),
                TrainingSpecification.dateFrom(fromDate),
                TrainingSpecification.dateTo(toDate),
                TrainingSpecification.traineeFirstName(traineeFirstName),
                TrainingSpecification.traineeLastName(traineeLastName)
        );
        return trainingRepository.findAll(spec);
    }
}
