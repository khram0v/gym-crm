package io.github.khram0v.gymcrm.service;

import io.github.khram0v.gymcrm.repository.TraineeRepository;
import io.github.khram0v.gymcrm.repository.TrainerRepository;
import io.github.khram0v.gymcrm.exception.AuthenticationException;
import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock private TraineeRepository traineeRepository;
    @Mock private TrainerRepository trainerRepository;

    @InjectMocks private AuthenticationService authenticationService;

    private Trainee trainee;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        trainee = new Trainee("John", "Doe", null, null);
        trainee.setUsername("John.Doe");
        trainee.setPassword("correctPass");
        trainee.setActive(true);

        trainer = new Trainer("Jane", "Smith", null);
        trainer.setUsername("Jane.Smith");
        trainer.setPassword("trainerPass");
        trainer.setActive(true);
    }

    // ~~~~~ Trainee authentication ~~~~~

    @Test
    void authenticateTrainee_whenCredentialsValid_passes() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        assertThatCode(() ->
                authenticationService.authenticateTrainee("John.Doe", "correctPass"))
                .doesNotThrowAnyException();
    }

    @Test
    void authenticateTrainee_whenWrongPassword_throws() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        assertThatThrownBy(() ->
                authenticationService.authenticateTrainee("John.Doe", "wrongPass"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void authenticateTrainee_whenUserNotFound_throws() {
        when(traineeRepository.findByUsername("Ghost.User")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authenticationService.authenticateTrainee("Ghost.User", "anyPass"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void authenticateTrainee_notFoundAndWrongPassword_produceSameMessage() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(traineeRepository.findByUsername("Ghost.User")).thenReturn(Optional.empty());

        String wrongPasswordMsg = catchMessage(() ->
                authenticationService.authenticateTrainee("John.Doe", "wrongPass"));
        String notFoundMsg = catchMessage(() ->
                authenticationService.authenticateTrainee("Ghost.User", "anyPass"));

        assertThat(wrongPasswordMsg).isEqualTo(notFoundMsg);
    }

    // ~~~~~ Trainer authentication ~~~~~

    @Test
    void authenticateTrainer_whenCredentialsValid_passes() {
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainer));

        assertThatCode(() ->
                authenticationService.authenticateTrainer("Jane.Smith", "trainerPass"))
                .doesNotThrowAnyException();
    }

    @Test
    void authenticateTrainer_whenWrongPassword_throws() {
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() ->
                authenticationService.authenticateTrainer("Jane.Smith", "wrongPass"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void authenticateTrainer_whenUserNotFound_throws() {
        when(trainerRepository.findByUsername("Ghost.Trainer")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authenticationService.authenticateTrainer("Ghost.Trainer", "anyPass"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");
    }

    private String catchMessage(Runnable action) {
        try {
            action.run();
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
