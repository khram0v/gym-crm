package io.github.khram0v.gymcrm.service.impl;

import io.github.khram0v.gymcrm.dto.response.TraineeTrainingResponse;
import io.github.khram0v.gymcrm.dto.response.TrainerTrainingResponse;
import io.github.khram0v.gymcrm.exception.ConflictException;
import io.github.khram0v.gymcrm.exception.NotFoundException;
import io.github.khram0v.gymcrm.mapper.TrainingMapper;
import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.model.Training;
import io.github.khram0v.gymcrm.model.TrainingType;
import io.github.khram0v.gymcrm.repository.TraineeRepository;
import io.github.khram0v.gymcrm.repository.TrainerRepository;
import io.github.khram0v.gymcrm.repository.TrainingRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @Mock private TrainingRepository trainingRepository;
    @Mock private TraineeRepository traineeRepository;
    @Mock private TrainerRepository trainerRepository;
    @Mock private TrainingMapper trainingMapper;
    @Spy private MeterRegistry meterRegistry = new SimpleMeterRegistry();
    @Spy private Clock clock = Clock.fixed(Instant.parse("2024-06-15T00:00:00Z"), ZoneOffset.UTC);

    @InjectMocks private TrainingServiceImpl trainingService;

    private TrainingType fitness;
    private Trainee trainee;
    private Trainer trainer;
    private Training trainingA;
    private Training trainingB;

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

        trainingA = new Training(trainee, trainer, "Morning Fitness",
                fitness, LocalDate.of(2024, Month.JUNE, 1), 60);
        trainingB = new Training(trainee, trainer, "Evening Fitness",
                fitness, LocalDate.of(2024, Month.JUNE, 2), 45);
    }

    // ~~~~~ addTraining ~~~~~

    @Test
    void addTraining_resolvesParties_derivesTypeFromTrainerSpecialization_andSaves() {
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainer));
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainingRepository.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));

        trainingService.addTraining(
                "Jane.Smith", "John.Doe", "Cardio Blast", LocalDate.of(2024, Month.JUNE, 1), 60);

        ArgumentCaptor<Training> captor = ArgumentCaptor.forClass(Training.class);
        verify(trainingRepository).save(captor.capture());
        Training saved = captor.getValue();
        assertThat(saved.getTrainingType()).isSameAs(fitness);
        assertThat(saved.getTrainingName()).isEqualTo("Cardio Blast");
        assertThat(saved.getTrainingDuration()).isEqualTo(60);
        assertThat(saved.getTrainer()).isSameAs(trainer);
        assertThat(saved.getTrainee()).isSameAs(trainee);

        assertThat(meterRegistry.get("gym.trainings.created").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("gym.trainings.created.duration").timer().count()).isEqualTo(1);
    }

    @Test
    void addTraining_whenTrainerNotFound_throwsAndDoesNotSave() {
        when(trainerRepository.findByUsername("Ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.addTraining(
                "Ghost", "John.Doe", "Cardio", LocalDate.now(), 60))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainer not found: Ghost");
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void addTraining_whenTraineeNotFound_throwsAndDoesNotSave() {
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainer));
        when(traineeRepository.findByUsername("Ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.addTraining(
                "Jane.Smith", "Ghost", "Cardio", LocalDate.now(), 60))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainee not found: Ghost");
        verify(trainingRepository, never()).save(any());
    }

    // ~~~~~ deleteTraining ~~~~~

    @Test
    void deleteTraining_whenTrainingDateInFuture_deletes_andIncrementsCounter() {
        Training futureTraining = new Training(trainee, trainer, "Cardio Blast",
                fitness, LocalDate.of(2024, Month.JUNE, 20), 60);
        when(trainingRepository.findById(10L)).thenReturn(Optional.of(futureTraining));

        trainingService.deleteTraining(10L);

        verify(trainingRepository).delete(futureTraining);
        assertThat(meterRegistry.get("gym.trainings.deleted").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("gym.trainings.deleted.duration").timer().count()).isEqualTo(1);
    }

    @Test
    void deleteTraining_whenTrainingDateIsToday_throwsConflict_andDoesNotDelete() {
        Training todayTraining = new Training(trainee, trainer, "Cardio Blast",
                fitness, LocalDate.of(2024, Month.JUNE, 15), 60);
        when(trainingRepository.findById(10L)).thenReturn(Optional.of(todayTraining));

        assertThatThrownBy(() -> trainingService.deleteTraining(10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already occurred");
        verify(trainingRepository, never()).delete(ArgumentMatchers.<Training>any());
    }

    @Test
    void deleteTraining_whenTrainingDateInPast_throwsConflict_andDoesNotDelete() {
        Training pastTraining = new Training(trainee, trainer, "Cardio Blast",
                fitness, LocalDate.of(2024, Month.JUNE, 10), 60);
        when(trainingRepository.findById(10L)).thenReturn(Optional.of(pastTraining));

        assertThatThrownBy(() -> trainingService.deleteTraining(10L))
                .isInstanceOf(ConflictException.class);
        verify(trainingRepository, never()).delete(ArgumentMatchers.<Training>any());
    }

    @Test
    void deleteTraining_whenNotFound_throwsAndDoesNotDelete() {
        when(trainingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.deleteTraining(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Training not found: 99");
        verify(trainingRepository, never()).delete(ArgumentMatchers.<Training>any());
    }

    // ~~~~~ getTraineeTrainings ~~~~~

    @Test
    void getTraineeTrainings_buildsSpec_queries_andMaps() {
        List<Training> found = List.of(trainingA, trainingB);
        List<TraineeTrainingResponse> stub = List.of(traineeTrainingStub(), traineeTrainingStub());
        when(trainingRepository.findAll(ArgumentMatchers.<Specification<Training>>any())).thenReturn(found);
        when(trainingMapper.toTraineeTrainingResponses(found)).thenReturn(stub);

        List<TraineeTrainingResponse> result = trainingService.getTraineeTrainings(
                "John.Doe", LocalDate.of(2024, Month.JANUARY, 1), LocalDate.of(2024, Month.DECEMBER, 31),
                "Jane", "Smith", "Fitness");

        assertThat(result).isSameAs(stub);
        verify(trainingMapper).toTraineeTrainingResponses(found);

        assertThat(meterRegistry.get("gym.trainings.query.duration").tag("type", "trainee").timer().count())
                .isEqualTo(1);
    }

    @Test
    void getTraineeTrainings_withNullCriteria_stillQueriesAndMaps() {
        when(trainingRepository.findAll(ArgumentMatchers.<Specification<Training>>any()))
                .thenReturn(List.of());
        when(trainingMapper.toTraineeTrainingResponses(anyList())).thenReturn(List.of());

        List<TraineeTrainingResponse> result = trainingService.getTraineeTrainings(
                "John.Doe", null, null, null, null, null);

        assertThat(result).isEmpty();
        verify(trainingRepository).findAll(ArgumentMatchers.<Specification<Training>>any());
    }

    // ~~~~~ getTrainerTrainings ~~~~~

    @Test
    void getTrainerTrainings_buildsSpec_queries_andMaps() {
        List<Training> found = List.of(trainingA);
        List<TrainerTrainingResponse> stub = List.of(trainerTrainingStub());
        when(trainingRepository.findAll(ArgumentMatchers.<Specification<Training>>any())).thenReturn(found);
        when(trainingMapper.toTrainerTrainingResponses(found)).thenReturn(stub);

        List<TrainerTrainingResponse> result = trainingService.getTrainerTrainings(
                "Jane.Smith", null, null, "John", "Doe");

        assertThat(result).isSameAs(stub);
        verify(trainingMapper).toTrainerTrainingResponses(found);

        assertThat(meterRegistry.get("gym.trainings.query.duration").tag("type", "trainer").timer().count())
                .isEqualTo(1);
    }

    @Test
    void getTrainerTrainings_withNullCriteria_stillQueriesAndMaps() {
        when(trainingRepository.findAll(ArgumentMatchers.<Specification<Training>>any()))
                .thenReturn(List.of());
        when(trainingMapper.toTrainerTrainingResponses(anyList())).thenReturn(List.of());

        List<TrainerTrainingResponse> result = trainingService.getTrainerTrainings(
                "Jane.Smith", null, null, null, null);

        assertThat(result).isEmpty();
        verify(trainingRepository).findAll(ArgumentMatchers.<Specification<Training>>any());
    }

    // ~~~~~ helpers ~~~~~

    private TraineeTrainingResponse traineeTrainingStub() {
        return new TraineeTrainingResponse(1L, "Morning Fitness",
                LocalDate.of(2024, Month.JUNE, 1), "Fitness", 60, "Jane", "Smith");
    }

    private TrainerTrainingResponse trainerTrainingStub() {
        return new TrainerTrainingResponse(1L, "Morning Fitness",
                LocalDate.of(2024, Month.JUNE, 1), "Fitness", 60, "John", "Doe");
    }
}
