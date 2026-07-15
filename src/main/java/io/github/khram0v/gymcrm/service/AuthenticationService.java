package io.github.khram0v.gymcrm.service;

import io.github.khram0v.gymcrm.repository.TraineeRepository;
import io.github.khram0v.gymcrm.repository.TrainerRepository;
import io.github.khram0v.gymcrm.exception.AuthenticationException;
import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthenticationService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    public AuthenticationService(TraineeRepository traineeRepository, TrainerRepository trainerRepository) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
    }

    @Transactional(readOnly = true)
    public void authenticateTrainee(String username, String password) {
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Authentication failed: trainee '{}' not found", username);
                    return new AuthenticationException("Invalid username or password");
                });
        if (!password.equals(trainee.getPassword())) {
            log.warn("Authentication failed: wrong password for trainee '{}'", username);
            throw new AuthenticationException("Invalid username or password");
        }
        log.debug("Trainee '{}' authenticated successfully", username);
    }

    @Transactional(readOnly = true)
    public void authenticateTrainer(String username, String password) {
        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Authentication failed: trainer '{}' not found", username);
                    return new AuthenticationException("Invalid username or password");
                });
        if (!password.equals(trainer.getPassword())) {
            log.warn("Authentication failed: wrong password for trainer '{}'", username);
            throw new AuthenticationException("Invalid username or password");
        }
        log.debug("Trainer '{}' authenticated successfully", username);
    }
}
