package io.github.khram0v.gymcrm.util;

import io.github.khram0v.gymcrm.repository.TraineeRepository;
import io.github.khram0v.gymcrm.repository.TrainerRepository;
import org.springframework.stereotype.Component;

@Component
public class UsernameRegistry {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    public UsernameRegistry(TraineeRepository traineeRepository, TrainerRepository trainerRepository) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
    }

    public boolean exists(String username) {
        return traineeRepository.existsByUsername(username) || trainerRepository.existsByUsername(username);
    }
}
