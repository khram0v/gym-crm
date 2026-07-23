package io.github.khram0v.gymcrm.service.impl;

import io.github.khram0v.gymcrm.repository.TraineeRepository;
import io.github.khram0v.gymcrm.repository.TrainerRepository;
import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.service.AuthService;
import io.github.khram0v.gymcrm.service.TraineeService;
import io.github.khram0v.gymcrm.util.UserCredentialsGenerator;
import io.github.khram0v.gymcrm.util.UsernameRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TraineeServiceImpl implements TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UserCredentialsGenerator credentialsGenerator;
    private final UsernameRegistry usernameRegistry;
    private final AuthService authService;

    @Override
    @Transactional
    public Trainee create(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        Trainee trainee = new Trainee(firstName, lastName, dateOfBirth, address);
        trainee.setUsername(credentialsGenerator.generateUsername(firstName, lastName, usernameRegistry::exists));
        trainee.setPassword(credentialsGenerator.generatePassword());
        trainee.setActive(true);

        Trainee saved = traineeRepository.save(trainee);
        log.info("Created trainee '{}'", saved.getUsername());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Trainee getByUsername(String username) {
        return traineeRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        authService.authenticate(username, oldPassword);

        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
        trainee.setPassword(newPassword);

        traineeRepository.save(trainee);
        log.info("Changed password for trainee '{}'", username);
    }

    @Override
    @Transactional
    public Trainee updateProfile(String username,
                                 String firstName,
                                 String lastName,
                                 LocalDate dateOfBirth,
                                 String address) {
        Trainee trainee = traineeRepository.findByUsername(username)
                        .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);

        Trainee saved = traineeRepository.save(trainee);
        log.info("Updated profile for trainee '{}'", username);
        return saved;
    }

    @Override
    @Transactional
    public void setActiveStatus(String username, boolean active) {
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));

        if (trainee.isActive() == active) {
            throw new IllegalArgumentException(
                    "Trainee '" + username + "' is already " + (active ? "active" : "inactive"));
        }
        trainee.setActive(active);

        traineeRepository.save(trainee);
        log.info("Set trainee '{}' active status to {}", username, active);
    }

    @Override
    @Transactional
    public void deleteByUsername(String username) {
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
        traineeRepository.delete(trainee);
        log.info("Deleted trainee '{}'", username);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Trainer> getUnassignedTrainers(String username) {
        if (!traineeRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Trainee not found: " + username);
        }

        return traineeRepository.findUnassignedTrainers(username);
    }

    @Override
    @Transactional
    public Set<Trainer> updateTrainers(String username, List<String> trainerUsernames) {
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));

        Set<Trainer> trainers = trainerUsernames.stream()
                .map(u -> trainerRepository.findByUsername(u)
                        .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + u)))
                .collect(Collectors.toSet());
        trainee.setTrainers(trainers);

        traineeRepository.save(trainee);
        log.info("Updated trainers list for trainee '{}' ({} trainers)",  username, trainers.size());
        return trainers;
    }
}
