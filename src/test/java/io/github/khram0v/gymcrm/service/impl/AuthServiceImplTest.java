package io.github.khram0v.gymcrm.service.impl;

import io.github.khram0v.gymcrm.exception.AuthenticationException;
import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.repository.TraineeRepository;
import io.github.khram0v.gymcrm.repository.TrainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private TraineeRepository traineeRepository;
    @Mock private TrainerRepository trainerRepository;

    @InjectMocks private AuthServiceImpl authService;

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

    @Test
    void authenticate_whenTraineeCredentialsValid_passes() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        assertThatCode(() -> authService.authenticate("John.Doe", "correctPass"))
                .doesNotThrowAnyException();
    }

    @Test
    void authenticate_whenTrainerCredentialsValid_passes() {
        when(traineeRepository.findByUsername("Jane.Smith")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainer));

        assertThatCode(() -> authService.authenticate("Jane.Smith", "trainerPass"))
                .doesNotThrowAnyException();
    }

    @Test
    void authenticate_whenWrongPassword_throws() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        assertThatThrownBy(() -> authService.authenticate("John.Doe", "wrongPass"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void authenticate_whenUserInNeitherPool_throws() {
        when(traineeRepository.findByUsername("Ghost.User")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("Ghost.User")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate("Ghost.User", "anyPass"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void authenticate_notFoundAndWrongPassword_produceSameMessage() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        Throwable wrongPassword = catchThrowable(() -> authService.authenticate("John.Doe", "wrongPass"));

        when(traineeRepository.findByUsername("Ghost.User")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("Ghost.User")).thenReturn(Optional.empty());
        Throwable notFound = catchThrowable(() -> authService.authenticate("Ghost.User", "anyPass"));

        assertThat(wrongPassword.getMessage()).isEqualTo(notFound.getMessage());
    }
}
