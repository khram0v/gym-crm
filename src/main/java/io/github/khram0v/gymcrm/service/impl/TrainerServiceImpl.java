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
import io.github.khram0v.gymcrm.security.PasswordVerifier;
import io.github.khram0v.gymcrm.service.TrainerService;
import io.github.khram0v.gymcrm.util.UserCredentialsGenerator;
import io.github.khram0v.gymcrm.util.UsernameRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final PasswordVerifier passwordVerifier;
    private final MeterRegistry meterRegistry;

    @Override
    @Transactional
    public RegistrationResponse create(String firstName, String lastName, Long specializationId) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            TrainingType specialization = trainingTypeRepository.findById(specializationId)
                    .orElseThrow(() -> new NotFoundException("Training type not found: " + specializationId));

            Trainer trainer = new Trainer(firstName, lastName, specialization);
            trainer.setUsername(credentialsGenerator.generateUsername(firstName, lastName, usernameRegistry::exists));

            String rawPassword = credentialsGenerator.generatePassword();
            trainer.setPassword(passwordEncoder.encode(rawPassword));
            trainer.setActive(true);

            Trainer saved = trainerRepository.save(trainer);
            log.info("Created trainer '{}'", saved.getUsername());
            meterRegistry.counter("gym.trainer.registrations").increment();
            return new RegistrationResponse(saved.getUsername(), rawPassword);
        } finally {
            sample.stop(meterRegistry.timer("gym.trainer.registration.duration"));
        }
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
        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));

        passwordVerifier.verify(oldPassword, trainer.getPassword());
        trainer.setPassword(passwordEncoder.encode(newPassword));

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
