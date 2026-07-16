package io.github.khram0v.gymcrm.facade;

import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.model.Training;
import io.github.khram0v.gymcrm.model.TrainingType;
import io.github.khram0v.gymcrm.service.TraineeService;
import io.github.khram0v.gymcrm.service.TrainerService;
import io.github.khram0v.gymcrm.service.TrainingService;
import io.github.khram0v.gymcrm.service.TrainingTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    @Mock private TraineeService traineeService;
    @Mock private TrainerService trainerService;
    @Mock private TrainingService trainingService;
    @Mock private TrainingTypeService trainingTypeService;

    @InjectMocks private GymFacade facade;

    // ~~~~~ Trainee delegation ~~~~~

    @Test
    void createTrainee_delegatesToTraineeService() {
        LocalDate dob = LocalDate.of(1995, Month.MAY, 20);
        facade.createTrainee("John", "Doe", dob, "123 Main St");

        verify(traineeService).create("John", "Doe", dob, "123 Main St");
    }

    @Test
    void getTrainee_delegatesAndReturns() {
        Trainee trainee = new Trainee("John", "Doe", null, null);
        when(traineeService.getByUsername("John.Doe", "pass")).thenReturn(trainee);

        assertThat(facade.getTrainee("John.Doe", "pass")).isSameAs(trainee);
        verify(traineeService).getByUsername("John.Doe", "pass");
    }

    @Test
    void changeTraineePassword_delegates() {
        facade.changeTraineePassword("John.Doe", "old", "new");
        verify(traineeService).changePassword("John.Doe", "old", "new");
    }

    @Test
    void updateTrainee_delegatesAndReturns() {
        LocalDate dob = LocalDate.of(1990, Month.MARCH, 3);
        Trainee updated = new Trainee("Johnny", "Doer", dob, "789 Pine Rd");
        when(traineeService.updateProfile("John.Doe", "pass", "Johnny", "Doer", dob, "789 Pine Rd"))
                .thenReturn(updated);

        assertThat(facade.updateTrainee("John.Doe", "pass", "Johnny", "Doer", dob, "789 Pine Rd"))
                .isSameAs(updated);
        verify(traineeService).updateProfile("John.Doe", "pass", "Johnny", "Doer", dob, "789 Pine Rd");
    }

    @Test
    void setTraineeActive_delegates() {
        facade.setTraineeActive("John.Doe", "pass", false);
        verify(traineeService).setActiveStatus("John.Doe", "pass", false);
    }

    @Test
    void deleteTrainee_delegates() {
        facade.deleteTrainee("John.Doe", "pass");
        verify(traineeService).deleteByUsername("John.Doe", "pass");
    }

    @Test
    void getUnassignedTrainers_delegatesAndReturns() {
        List<Trainer> trainers = List.of(new Trainer("Jane", "Smith", new TrainingType("Fitness")));
        when(traineeService.getUnassignedTrainers("John.Doe", "pass")).thenReturn(trainers);

        assertThat(facade.getUnassignedTrainers("John.Doe", "pass")).isEqualTo(trainers);
        verify(traineeService).getUnassignedTrainers("John.Doe", "pass");
    }

    @Test
    void updateTraineeTrainers_delegatesAndReturns() {
        List<String> usernames = List.of("Jane.Smith", "Bob.Jones");
        List<Trainer> trainers = List.of(new Trainer("Jane", "Smith", new TrainingType("Fitness")));
        when(traineeService.updateTrainers("John.Doe", "pass", usernames)).thenReturn(trainers);

        assertThat(facade.updateTraineeTrainers("John.Doe", "pass", usernames)).isEqualTo(trainers);
        verify(traineeService).updateTrainers("John.Doe", "pass", usernames);
    }

    // ~~~~~ Trainer delegation ~~~~~

    @Test
    void createTrainer_delegatesToTrainerService() {
        facade.createTrainer("Jane", "Smith", 1L);
        verify(trainerService).create("Jane", "Smith", 1L);
    }

    @Test
    void getTrainer_delegatesAndReturns() {
        Trainer trainer = new Trainer("Jane", "Smith", new TrainingType("Fitness"));
        when(trainerService.getByUsername("Jane.Smith", "pass")).thenReturn(trainer);

        assertThat(facade.getTrainer("Jane.Smith", "pass")).isSameAs(trainer);
        verify(trainerService).getByUsername("Jane.Smith", "pass");
    }

    @Test
    void changeTrainerPassword_delegates() {
        facade.changeTrainerPassword("Jane.Smith", "old", "new");
        verify(trainerService).changePassword("Jane.Smith", "old", "new");
    }

    @Test
    void updateTrainer_delegatesAndReturns() {
        Trainer updated = new Trainer("Janet", "Smithers", new TrainingType("Yoga"));
        when(trainerService.updateProfile("Jane.Smith", "pass", "Janet", "Smithers", 2L))
                .thenReturn(updated);

        assertThat(facade.updateTrainer("Jane.Smith", "pass", "Janet", "Smithers", 2L))
                .isSameAs(updated);
        verify(trainerService).updateProfile("Jane.Smith", "pass", "Janet", "Smithers", 2L);
    }

    @Test
    void setTrainerActive_delegates() {
        facade.setTrainerActive("Jane.Smith", "pass", true);
        verify(trainerService).setActiveStatus("Jane.Smith", "pass", true);
    }

    // ~~~~~ Training delegation ~~~~~

    @Test
    void addTraining_delegatesToTrainingService() {
        LocalDate date = LocalDate.of(2026, Month.JANUARY, 15);
        facade.addTraining("Jane.Smith", "pass", "John.Doe", "Session", 1L, date, 60);

        verify(trainingService).addTraining("Jane.Smith", "pass", "John.Doe", "Session", 1L, date, 60);
    }

    @Test
    void getTraineeTrainings_delegatesAndReturns() {
        LocalDate from = LocalDate.of(2026, Month.JANUARY, 1);
        LocalDate to = LocalDate.of(2026, Month.DECEMBER, 31);
        List<Training> trainings = List.of();
        when(trainingService.getTraineeTrainings("John.Doe", "pass", from, to, "Jane", "Smith", "Fitness"))
                .thenReturn(trainings);

        assertThat(facade.getTraineeTrainings("John.Doe", "pass", from, to, "Jane", "Smith", "Fitness"))
                .isEqualTo(trainings);
        verify(trainingService).getTraineeTrainings("John.Doe", "pass", from, to, "Jane", "Smith", "Fitness");
    }

    @Test
    void getTrainerTrainings_delegatesAndReturns() {
        LocalDate from = LocalDate.of(2026, Month.JANUARY, 1);
        LocalDate to = LocalDate.of(2026, Month.DECEMBER, 31);
        List<Training> trainings = List.of();
        when(trainingService.getTrainerTrainings("Jane.Smith", "pass", from, to, "John", "Doe"))
                .thenReturn(trainings);

        assertThat(facade.getTrainerTrainings("Jane.Smith", "pass", from, to, "John", "Doe"))
                .isEqualTo(trainings);
        verify(trainingService).getTrainerTrainings("Jane.Smith", "pass", from, to, "John", "Doe");
    }

    // ~~~~~ Training type delegation ~~~~~

    @Test
    void getAllTrainingTypes_delegatesAndReturns() {
        List<TrainingType> types = List.of(new TrainingType("Fitness"), new TrainingType("Yoga"));
        when(trainingTypeService.getAll()).thenReturn(types);

        assertThat(facade.getAllTrainingTypes()).isEqualTo(types);
        verify(trainingTypeService).getAll();
    }
}
