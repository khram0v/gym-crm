package io.github.khram0v.gymcrm.service.impl;

import io.github.khram0v.gymcrm.repository.TraineeRepository;
import io.github.khram0v.gymcrm.repository.TrainerRepository;
import io.github.khram0v.gymcrm.exception.AuthenticationException;
import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.service.AuthService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final MeterRegistry meterRegistry;

    @Override
    @Transactional(readOnly = true)
    public void authenticate(String username, String password) {
        try {
            String storedPassword = traineeRepository.findByUsername(username)
                    .map(Trainee::getPassword)
                    .or(() -> trainerRepository.findByUsername(username).map(Trainer::getPassword))
                    .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

            if (!storedPassword.equals(password)) {
                throw new AuthenticationException("Invalid username or password");
            }

            meterRegistry.counter("gym.auth.attempts", "result", "success").increment();
        } catch (AuthenticationException e) {
            meterRegistry.counter("gym.auth.attempts", "result", "failure").increment();
            throw e;
        }
    }
}
