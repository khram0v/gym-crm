package io.github.khram0v.gymcrm.service;

import io.github.khram0v.gymcrm.exception.AuthenticationException;
import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.model.Training;
import io.github.khram0v.gymcrm.model.TrainingType;
import io.github.khram0v.gymcrm.repository.TraineeRepository;
import io.github.khram0v.gymcrm.repository.TrainerRepository;
import io.github.khram0v.gymcrm.repository.TrainingRepository;
import io.github.khram0v.gymcrm.repository.TrainingTypeRepository;
import io.github.khram0v.gymcrm.validation.EntityValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock private TrainingRepository trainingRepository;
    @Mock private TraineeRepository traineeRepository;
    @Mock private TrainerRepository trainerRepository;
    @Mock private TrainingTypeRepository trainingTypeRepository;
    @Mock private AuthenticationService authenticationService;
    @Mock private EntityValidator validator;

    @InjectMocks private TrainingService trainingService;

    private Trainee trainee;
    private Trainer trainer;
    private TrainingType fitness;

    @BeforeEach
    void setUp() {
        fitness = new TrainingType("Fitness");

        trainee = new Trainee("John", "Doe", LocalDate.of(1995, Month.MAY, 20), "123 Main St");
        trainee.setUsername("John.Doe");
        trainee.setPassword("traineePass");
        trainee.setActive(true);

        trainer = new Trainer("Jane", "Smith", fitness);
        trainer.setUsername("Jane.Smith");
        trainer.setPassword("trainerPass");
        trainer.setActive(true);
    }

    // ~~~~~ Add training ~~~~~

    @Test
    void addTraining_authenticatesTrainer_resolvesEntities_validates_andSaves() {
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainer));
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(fitness));
        when(trainingRepository.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));

        Training result = trainingService.addTraining("Jane.Smith", "trainerPass",
                "John.Doe", "Morning Session", 1L, LocalDate.of(2026, Month.JANUARY, 15), 60);

        assertThat(result.getTrainee()).isSameAs(trainee);
        assertThat(result.getTrainer()).isSameAs(trainer);
        assertThat(result.getTrainingType()).isSameAs(fitness);
        assertThat(result.getTrainingName()).isEqualTo("Morning Session");
        assertThat(result.getTrainingDate()).isEqualTo(LocalDate.of(2026, Month.JANUARY, 15));
        assertThat(result.getTrainingDuration()).isEqualTo(60);

        verify(authenticationService).authenticateTrainer("Jane.Smith", "trainerPass");
        verify(authenticationService, never()).authenticateTrainee(any(), any());
    }

    @Test
    void addTraining_validatesBeforeSaving() {
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainer));
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(fitness));
        when(trainingRepository.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));

        trainingService.addTraining("Jane.Smith", "trainerPass",
                "John.Doe", "Morning Session", 1L, LocalDate.of(2026, Month.JANUARY, 15), 60);

        InOrder order = inOrder(validator, trainingRepository);
        order.verify(validator).validate(any(Training.class));
        order.verify(trainingRepository).save(any(Training.class));
    }

    @Test
    void addTraining_whenAuthFails_neverResolvesOrSaves() {
        doThrow(new AuthenticationException("Invalid username or password"))
                .when(authenticationService).authenticateTrainer("Jane.Smith", "wrong");

        assertThatThrownBy(() -> trainingService.addTraining("Jane.Smith", "wrong",
                "John.Doe", "Morning Session", 1L, LocalDate.of(2026, Month.JANUARY, 15), 60))
                .isInstanceOf(AuthenticationException.class);

        verify(trainerRepository, never()).findByUsername(any());
        verify(traineeRepository, never()).findByUsername(any());
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void addTraining_whenTraineeNotFound_throwsAndDoesNotSave() {
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainer));
        when(traineeRepository.findByUsername("Ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.addTraining("Jane.Smith", "trainerPass",
                "Ghost", "Morning Session", 1L, LocalDate.of(2026, Month.JANUARY, 15), 60))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ghost");

        verify(trainingRepository, never()).save(any());
    }

    @Test
    void addTraining_whenTrainerNotFound_throwsAndDoesNotSave() {
        when(trainerRepository.findByUsername("Ghost.Trainer")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.addTraining("Ghost.Trainer", "trainerPass",
                "John.Doe", "Morning Session", 1L, LocalDate.of(2026, Month.JANUARY, 15), 60))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ghost.Trainer");

        verify(trainingRepository, never()).save(any());
    }

    @Test
    void addTraining_whenTrainingTypeNotFound_throwsAndDoesNotSave() {
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainer));
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainingTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.addTraining("Jane.Smith", "trainerPass",
                "John.Doe", "Morning Session", 99L, LocalDate.of(2026, Month.JANUARY, 15), 60))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");

        verify(trainingRepository, never()).save(any());
    }

    // ~~~~~ Trainee trainings ~~~~~

    @Test
    void getTraineeTrainings_authenticatesTrainee_thenQueriesRepository() {
        Training t1 = buildTraining();
        when(trainingRepository.findAll(ArgumentMatchers.<Specification<Training>>any()))
                .thenReturn(List.of(t1));

        List<Training> result = trainingService.getTraineeTrainings("John.Doe", "traineePass",
                LocalDate.of(2026, Month.JANUARY, 1), LocalDate.of(2026, Month.DECEMBER, 31),
                "Jane", "Smith", "Fitness");

        assertThat(result).containsExactly(t1);
        verify(authenticationService).authenticateTrainee("John.Doe", "traineePass");
        verify(trainingRepository).findAll(ArgumentMatchers.<Specification<Training>>any());
    }

    @Test
    void getTraineeTrainings_withNullCriteria_stillQueries() {
        when(trainingRepository.findAll(ArgumentMatchers.<Specification<Training>>any()))
                .thenReturn(List.of());

        List<Training> result = trainingService.getTraineeTrainings("John.Doe", "traineePass",
                null, null, null, null, null);

        assertThat(result).isEmpty();
        verify(trainingRepository).findAll(ArgumentMatchers.<Specification<Training>>any());
    }

    @Test
    void getTraineeTrainings_whenAuthFails_neverQueries() {
        doThrow(new AuthenticationException("Invalid username or password"))
                .when(authenticationService).authenticateTrainee("John.Doe", "wrong");

        assertThatThrownBy(() -> trainingService.getTraineeTrainings("John.Doe", "wrong",
                null, null, null, null, null))
                .isInstanceOf(AuthenticationException.class);

        verify(trainingRepository, never()).findAll(ArgumentMatchers.<Specification<Training>>any());
    }

    // ~~~~~ Trainer trainings ~~~~~

    @Test
    void getTrainerTrainings_authenticatesTrainer_thenQueriesRepository() {
        Training t1 = buildTraining();
        when(trainingRepository.findAll(ArgumentMatchers.<Specification<Training>>any()))
                .thenReturn(List.of(t1));

        List<Training> result = trainingService.getTrainerTrainings("Jane.Smith", "trainerPass",
                LocalDate.of(2026, Month.JANUARY, 1), LocalDate.of(2026, Month.DECEMBER, 31),
                "John", "Doe");

        assertThat(result).containsExactly(t1);
        verify(authenticationService).authenticateTrainer("Jane.Smith", "trainerPass");
        verify(trainingRepository).findAll(ArgumentMatchers.<Specification<Training>>any());
    }

    @Test
    void getTrainerTrainings_withNullCriteria_stillQueries() {
        when(trainingRepository.findAll(ArgumentMatchers.<Specification<Training>>any()))
                .thenReturn(List.of());

        List<Training> result = trainingService.getTrainerTrainings("Jane.Smith", "trainerPass",
                null, null, null, null);

        assertThat(result).isEmpty();
        verify(trainingRepository).findAll(ArgumentMatchers.<Specification<Training>>any());
    }

    @Test
    void getTrainerTrainings_whenAuthFails_neverQueries() {
        doThrow(new AuthenticationException("Invalid username or password"))
                .when(authenticationService).authenticateTrainer("Jane.Smith", "wrong");

        assertThatThrownBy(() -> trainingService.getTrainerTrainings("Jane.Smith", "wrong",
                null, null, null, null))
                .isInstanceOf(AuthenticationException.class);

        verify(trainingRepository, never()).findAll(ArgumentMatchers.<Specification<Training>>any());
    }

    private Training buildTraining() {
        return new Training(trainee, trainer, "Morning Session", fitness,
                LocalDate.of(2026, Month.JANUARY, 15), 60);
    }
}
