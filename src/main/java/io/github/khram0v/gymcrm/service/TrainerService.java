package io.github.khram0v.gymcrm.service;

import io.github.khram0v.gymcrm.repository.TrainerRepository;
import io.github.khram0v.gymcrm.repository.TrainingTypeRepository;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.model.TrainingType;
import io.github.khram0v.gymcrm.util.UserCredentialsGenerator;
import io.github.khram0v.gymcrm.util.UsernameRegistry;
import io.github.khram0v.gymcrm.validation.EntityValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final AuthenticationService authenticationService;
    private final UserCredentialsGenerator credentialsGenerator;
    private final UsernameRegistry usernameRegistry;
    private final EntityValidator validator;

    public TrainerService(TrainerRepository trainerRepository,
                          TrainingTypeRepository trainingTypeRepository,
                          AuthenticationService authenticationService,
                          UserCredentialsGenerator credentialsGenerator,
                          UsernameRegistry usernameRegistry,
                          EntityValidator validator) {
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.authenticationService = authenticationService;
        this.credentialsGenerator = credentialsGenerator;
        this.usernameRegistry = usernameRegistry;
        this.validator = validator;
    }

    @Transactional
    public Trainer create(String firstName, String lastName, Long specializationId) {
        TrainingType specialization = trainingTypeRepository.findById(specializationId)
                .orElseThrow(() -> new IllegalArgumentException("Training type not found: id=" + specializationId));

        Trainer trainer = new Trainer(firstName, lastName, specialization);
        trainer.setUsername(credentialsGenerator.generateUsername(firstName, lastName, usernameRegistry::exists));
        trainer.setPassword(credentialsGenerator.generatePassword());
        trainer.setActive(true);

        validator.validate(trainer);
        Trainer saved =  trainerRepository.save(trainer);
        log.info("Created trainer '{}' (id={})", saved.getUsername(), saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Trainer getByUsername(String username, String password) {
        authenticationService.authenticateTrainer(username, password);
        return trainerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        authenticationService.authenticateTrainer(username, oldPassword);
        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));
        trainer.setPassword(newPassword);
        validator.validate(trainer);
        trainerRepository.save(trainer);
        log.info("Password changed for trainer '{}'", username);
    }

    @Transactional
    public Trainer updateProfile(String username,
                                 String password,
                                 String firstName,
                                 String lastName,
                                 Long specializationId) {
        authenticationService.authenticateTrainer(username, password);
        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));

        TrainingType specialization = trainingTypeRepository.findById(specializationId)
                .orElseThrow(() -> new IllegalArgumentException("Training type not found: id=" + specializationId));

        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setSpecialization(specialization);
        validator.validate(trainer);
        Trainer updated = trainerRepository.save(trainer);
        log.info("Updated profile for trainer '{}'", username);
        return updated;
    }

    @Transactional
    public void setActiveStatus(String username, String password, boolean active) {
        authenticationService.authenticateTrainer(username, password);
        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));

        if (trainer.isActive() == active) {
            throw new IllegalArgumentException(
                    "Trainer '" + username + "' is already " + (active ? "active" : "inactive"));
        }

        trainer.setActive(active);
        trainerRepository.save(trainer);
        log.info("Trainer '{}' active status set to {}", username, active);
    }
}
