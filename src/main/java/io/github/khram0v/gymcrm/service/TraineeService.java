package io.github.khram0v.gymcrm.service;

import io.github.khram0v.gymcrm.repository.TraineeRepository;
import io.github.khram0v.gymcrm.repository.TrainerRepository;
import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.util.UserCredentialsGenerator;
import io.github.khram0v.gymcrm.util.UsernameRegistry;
import io.github.khram0v.gymcrm.validation.EntityValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final AuthenticationService authenticationService;
    private final UserCredentialsGenerator  credentialsGenerator;
    private final UsernameRegistry usernameRegistry;
    private final EntityValidator validator;

    public TraineeService(TraineeRepository traineeRepository,
                          TrainerRepository trainerRepository,
                          AuthenticationService authenticationService,
                          UserCredentialsGenerator credentialsGenerator,
                          UsernameRegistry usernameRegistry,
                          EntityValidator validator) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.authenticationService = authenticationService;
        this.credentialsGenerator = credentialsGenerator;
        this.usernameRegistry = usernameRegistry;
        this.validator = validator;
    }

    @Transactional
    public Trainee create(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        Trainee trainee = new Trainee(firstName, lastName, dateOfBirth, address);
        trainee.setUsername(credentialsGenerator.generateUsername(firstName, lastName, usernameRegistry::exists));
        trainee.setPassword(credentialsGenerator.generatePassword());
        trainee.setActive(true);

        validator.validate(trainee);
        Trainee saved = traineeRepository.save(trainee);
        log.info("Created trainee '{}' (id={})", saved.getUsername(), saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Trainee getByUsername(String username, String password) {
        authenticationService.authenticateTrainee(username, password);
        return traineeRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        authenticationService.authenticateTrainee(username, oldPassword);
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
        trainee.setPassword(newPassword);
        validator.validate(trainee);
        traineeRepository.save(trainee);
        log.info("Password changed for trainee '{}'", username);
    }

    @Transactional
    public Trainee updateProfile(String username,
                                 String password,
                                 String firstName,
                                 String lastName,
                                 LocalDate dateOfBirth,
                                 String address) {
        authenticationService.authenticateTrainee(username, password);
        Trainee trainee = traineeRepository.findByUsername(username)
                        .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);
        validator.validate(trainee);
        Trainee updated = traineeRepository.save(trainee);
        log.info("Updated profile for trainee '{}'", username);
        return updated;
    }

    @Transactional
    public void setActiveStatus(String username, String password, boolean active) {
        authenticationService.authenticateTrainee(username, password);
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));

        if (trainee.isActive() == active) {
            throw new IllegalArgumentException(
                    "Trainee '" + username + "' is already " + (active ? "active" : "inactive"));
        }

        trainee.setActive(active);
        traineeRepository.save(trainee);
        log.info("Trainee '{}' active status set to {}", username, active);
    }

    @Transactional
    public void deleteByUsername(String username, String password) {
        authenticationService.authenticateTrainee(username, password);
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
        traineeRepository.delete(trainee);
        log.info("Deleted trainee '{}' (cascade removed trainings)", username);
    }

    @Transactional
    public List<Trainer> updateTrainers(String username, String password, List<String> trainerUsernames) {
        authenticationService.authenticateTrainee(username, password);
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));

        List<Trainer> trainers = trainerUsernames.stream()
                .map(u -> trainerRepository.findByUsername(u)
                        .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + u)))
                .toList();

        trainee.setTrainers(new ArrayList<>(trainers));
        traineeRepository.save(trainee);
        log.info("Updated trainers list for trainee '{}' ({} trainers)",  username, trainers.size());
        return trainers;
    }

    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String username, String password) {
        authenticationService.authenticateTrainee(username, password);
        return traineeRepository.findUnassignedTrainers(username);
    }
}
