package io.github.khram0v.gymcrm.service.impl;

import io.github.khram0v.gymcrm.dto.response.TraineeTrainingResponse;
import io.github.khram0v.gymcrm.dto.response.TrainerTrainingResponse;
import io.github.khram0v.gymcrm.exception.NotFoundException;
import io.github.khram0v.gymcrm.mapper.TrainingMapper;
import io.github.khram0v.gymcrm.repository.TraineeRepository;
import io.github.khram0v.gymcrm.repository.TrainerRepository;
import io.github.khram0v.gymcrm.repository.TrainingRepository;
import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.model.Training;
import io.github.khram0v.gymcrm.repository.specification.TrainingSpecification;
import io.github.khram0v.gymcrm.service.TrainingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final TrainingMapper trainingMapper;

    @Override
    @Transactional
    public void addTraining(String trainerUsername,
                                String traineeUsername,
                                String trainingName,
                                LocalDate trainingDate,
                                Integer duration) {
        Trainer trainer = trainerRepository.findByUsername(trainerUsername)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + trainerUsername));
        Trainee trainee = traineeRepository.findByUsername(traineeUsername)
                .orElseThrow(() -> new NotFoundException("Trainee not found: " + traineeUsername));

        Training training = new Training(trainee, trainer, trainingName,
                trainer.getSpecialization(), trainingDate, duration);

        trainingRepository.save(training);
        log.info("Added training '{}' (trainer '{}', trainee '{}')", trainingName, trainerUsername, traineeUsername);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TraineeTrainingResponse> getTraineeTrainings(String traineeUsername,
                                                             LocalDate from,
                                                             LocalDate to,
                                                             String trainerFirstName,
                                                             String trainerLastName,
                                                             String trainingTypeName) {
        Specification<Training> spec = Specification.allOf(
                TrainingSpecification.hasTraineeUsername(traineeUsername),
                TrainingSpecification.dateFrom(from),
                TrainingSpecification.dateTo(to),
                TrainingSpecification.trainerFirstName(trainerFirstName),
                TrainingSpecification.trainerLastName(trainerLastName),
                TrainingSpecification.trainingTypeName(trainingTypeName)
        );
        return trainingMapper.toTraineeTrainingResponses(trainingRepository.findAll(spec));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerTrainingResponse> getTrainerTrainings(String trainerUsername,
                                                             LocalDate from,
                                                             LocalDate to,
                                                             String traineeFirstName,
                                                             String traineeLastName) {
        Specification<Training> spec = Specification.allOf(
                TrainingSpecification.hasTrainerUsername(trainerUsername),
                TrainingSpecification.dateFrom(from),
                TrainingSpecification.dateTo(to),
                TrainingSpecification.traineeFirstName(traineeFirstName),
                TrainingSpecification.traineeLastName(traineeLastName)
        );
        return trainingMapper.toTrainerTrainingResponses(trainingRepository.findAll(spec));
    }
}
