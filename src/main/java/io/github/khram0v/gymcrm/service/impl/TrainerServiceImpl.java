package io.github.khram0v.gymcrm.service.impl;

import io.github.khram0v.gymcrm.dto.response.RegistrationResponse;
import io.github.khram0v.gymcrm.dto.response.TrainerProfileResponse;
import io.github.khram0v.gymcrm.exception.ConflictException;
import io.github.khram0v.gymcrm.exception.NotFoundException;
import io.github.khram0v.gymcrm.mapper.TrainerMapper;
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
    private final TrainerMapper trainerMapper;
    private final AuthService authService;

    @Override
    @Transactional
    public RegistrationResponse create(String firstName, String lastName, Long specializationId) {
        TrainingType specialization = trainingTypeRepository.findById(specializationId)
                .orElseThrow(() -> new NotFoundException("Training type not found: " + specializationId));

        Trainer trainer = new Trainer(firstName, lastName, specialization);
        trainer.setUsername(credentialsGenerator.generateUsername(firstName, lastName, usernameRegistry::exists));
        trainer.setPassword(credentialsGenerator.generatePassword());
        trainer.setActive(true);

        Trainer saved = trainerRepository.save(trainer);
        log.info("Created trainer '{}'", saved.getUsername());
        return trainerMapper.toRegistrationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerProfileResponse getByUsername(String username) {
        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));
        return trainerMapper.toProfileResponse(trainer);
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        authService.authenticate(username, oldPassword);

        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));
        trainer.setPassword(newPassword);

        trainerRepository.save(trainer);
        log.info("Changed password for trainer '{}'", username);
    }

    @Override
    @Transactional
    public TrainerProfileResponse updateProfile(String username, String firstName, String lastName, boolean active) {
        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setActive(active);

        Trainer saved = trainerRepository.save(trainer);
        log.info("Updated profile for trainer '{}'", username);
        return trainerMapper.toProfileResponse(saved);
    }

    @Override
    @Transactional
    public void setActiveStatus(String username, boolean active) {
        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));

        if (trainer.isActive() == active) {
            throw new ConflictException(
                    "Trainer '" + username + "' is already " + (active ? "active" : "inactive"));
        }
        trainer.setActive(active);

        trainerRepository.save(trainer);
        log.info("Set trainer '{}' active status to {}", username, active);
    }
}
