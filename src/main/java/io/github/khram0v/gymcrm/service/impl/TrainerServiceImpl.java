package io.github.khram0v.gymcrm.service.impl;

import io.github.khram0v.gymcrm.repository.TrainerRepository;
import io.github.khram0v.gymcrm.repository.TrainingTypeRepository;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.model.TrainingType;
import io.github.khram0v.gymcrm.service.AuthService;
import io.github.khram0v.gymcrm.service.TrainerService;
import io.github.khram0v.gymcrm.util.UserCredentialsGenerator;
import io.github.khram0v.gymcrm.util.UsernameRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UserCredentialsGenerator credentialsGenerator;
    private final UsernameRegistry usernameRegistry;
    private final AuthService authService;

    @Override
    @Transactional
    public Trainer create(String firstName, String lastName, Long specializationId) {
        TrainingType specialization = trainingTypeRepository.findById(specializationId)
                .orElseThrow(() -> new IllegalArgumentException("Training type not found: " + specializationId));

        Trainer trainer = new Trainer(firstName, lastName, specialization);
        trainer.setUsername(credentialsGenerator.generateUsername(firstName, lastName, usernameRegistry::exists));
        trainer.setPassword(credentialsGenerator.generatePassword());
        trainer.setActive(true);

        Trainer saved = trainerRepository.save(trainer);
        log.info("Created trainer '{}'", saved.getUsername());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Trainer getByUsername(String username) {
        return trainerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        authService.authenticate(username, oldPassword);

        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));
        trainer.setPassword(newPassword);

        trainerRepository.save(trainer);
        log.info("Changed password for trainer '{}'", username);
    }

    @Override
    @Transactional
    public Trainer updateProfile(String username, String firstName, String lastName, Long specializationId) {
        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));
        TrainingType specialization = trainingTypeRepository.findById(specializationId)
                .orElseThrow(() -> new IllegalArgumentException("Training type not found: " + specializationId));
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setSpecialization(specialization);

        Trainer saved = trainerRepository.save(trainer);
        log.info("Updated profile for trainer '{}'", username);
        return saved;
    }

    @Override
    @Transactional
    public void setActiveStatus(String username, boolean active) {
        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));

        if (trainer.isActive() == active) {
            throw new IllegalArgumentException(
                    "Trainer '" + username + "' is already " + (active ? "active" : "inactive"));
        }
        trainer.setActive(active);

        trainerRepository.save(trainer);
        log.info("Set trainer '{}' active status to {}", username, active);
    }
}
