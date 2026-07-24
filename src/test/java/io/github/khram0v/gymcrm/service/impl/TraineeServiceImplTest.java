package io.github.khram0v.gymcrm.service.impl;

import io.github.khram0v.gymcrm.exception.AuthenticationException;
import io.github.khram0v.gymcrm.exception.ConflictException;
import io.github.khram0v.gymcrm.exception.NotFoundException;
import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.model.TrainingType;
import io.github.khram0v.gymcrm.repository.TraineeRepository;
import io.github.khram0v.gymcrm.repository.TrainerRepository;
import io.github.khram0v.gymcrm.service.AuthService;
import io.github.khram0v.gymcrm.util.UserCredentialsGenerator;
import io.github.khram0v.gymcrm.util.UsernameRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @Mock private TraineeRepository traineeRepository;
    @Mock private TrainerRepository trainerRepository;
    @Mock private UserCredentialsGenerator credentialsGenerator;
    @Mock private UsernameRegistry usernameRegistry;
    @Mock private AuthService authService;

    @InjectMocks private TraineeServiceImpl traineeService;

    private Trainee existingTrainee;

    @BeforeEach
    void setUp() {
        existingTrainee = new Trainee("John", "Doe",
                LocalDate.of(1995, Month.MAY, 20), "123 Main St");
        existingTrainee.setUsername("John.Doe");
        existingTrainee.setPassword("currentPass");
        existingTrainee.setActive(true);
    }

    // ~~~~~ create ~~~~~

    @Test
    void create_generatesCredentials_activates_andSaves() {
        when(credentialsGenerator.generateUsername(eq("Alan"), eq("Poe"), any()))
                .thenReturn("Alan.Poe");
        when(credentialsGenerator.generatePassword()).thenReturn("pass123");
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainee result = traineeService.create("Alan", "Poe",
                LocalDate.of(2000, Month.JANUARY, 1), "Main St");

        assertThat(result.getUsername()).isEqualTo("Alan.Poe");
        assertThat(result.getPassword()).isEqualTo("pass123");
        assertThat(result.isActive()).isTrue();
        verify(traineeRepository).save(any(Trainee.class));
    }

    // ~~~~~ getByUsername ~~~~~

    @Test
    void getByUsername_whenExists_returnsTrainee() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));

        assertThat(traineeService.getByUsername("John.Doe")).isSameAs(existingTrainee);
    }

    @Test
    void getByUsername_whenNotFound_throws() {
        when(traineeRepository.findByUsername("Ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.getByUsername("Ghost"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainee not found: Ghost");
    }

    // ~~~~~ changePassword ~~~~~

    @Test
    void changePassword_authenticatesOldPassword_thenUpdates() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));

        traineeService.changePassword("John.Doe", "currentPass", "newPass");

        verify(authService).authenticate("John.Doe", "currentPass");
        assertThat(existingTrainee.getPassword()).isEqualTo("newPass");
        verify(traineeRepository).save(existingTrainee);
    }

    @Test
    void changePassword_whenOldPasswordInvalid_neverUpdates() {
        doThrow(new AuthenticationException("Invalid username or password"))
                .when(authService).authenticate("John.Doe", "wrongPass");

        assertThatThrownBy(() -> traineeService.changePassword("John.Doe", "wrongPass", "newPass"))
                .isInstanceOf(AuthenticationException.class);

        verify(traineeRepository, never()).findByUsername(anyString());
        verify(traineeRepository, never()).save(any());
    }

    // ~~~~~ updateProfile ~~~~~

    @Test
    void updateProfile_updatesFieldsIncludingActive_andSaves() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainee result = traineeService.updateProfile(
                "John.Doe", "Johnny", "Doe", LocalDate.of(1999, Month.MAY, 5), "New Address", false);

        assertThat(result.getFirstName()).isEqualTo("Johnny");
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1999, Month.MAY, 5));
        assertThat(result.getAddress()).isEqualTo("New Address");
        assertThat(result.isActive()).isFalse();
        verify(traineeRepository).save(existingTrainee);
    }

    @Test
    void updateProfile_whenNotFound_throwsAndDoesNotSave() {
        when(traineeRepository.findByUsername("Ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.updateProfile("Ghost", "A", "B", null, null, true))
                .isInstanceOf(NotFoundException.class);
        verify(traineeRepository, never()).save(any());
    }

    // ~~~~~ setActiveStatus ~~~~~

    @Test
    void setActiveStatus_whenChanging_updates() {
        existingTrainee.setActive(false);
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));

        traineeService.setActiveStatus("John.Doe", true);

        assertThat(existingTrainee.isActive()).isTrue();
        verify(traineeRepository).save(existingTrainee);
    }

    @Test
    void setActiveStatus_whenAlreadyInState_throwsAndDoesNotSave() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));

        assertThatThrownBy(() -> traineeService.setActiveStatus("John.Doe", true))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already active");
        verify(traineeRepository, never()).save(any());
    }

    // ~~~~~ deleteByUsername ~~~~~

    @Test
    void deleteByUsername_whenExists_deletes() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));

        traineeService.deleteByUsername("John.Doe");

        verify(traineeRepository).delete(existingTrainee);
    }

    @Test
    void deleteByUsername_whenNotFound_throwsAndDoesNotDelete() {
        when(traineeRepository.findByUsername("Ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.deleteByUsername("Ghost"))
                .isInstanceOf(NotFoundException.class);
        verify(traineeRepository, never()).delete(any());
    }

    // ~~~~~ getUnassignedTrainers ~~~~~

    @Test
    void getUnassignedTrainers_whenTraineeExists_returnsFromQuery() {
        when(traineeRepository.existsByUsername("John.Doe")).thenReturn(true);
        Trainer t1 = new Trainer("Ann", "Lee", new TrainingType("Yoga"));
        Trainer t2 = new Trainer("Bob", "Fox", new TrainingType("Fitness"));
        when(traineeRepository.findUnassignedTrainers("John.Doe")).thenReturn(Set.of(t1, t2));

        assertThat(traineeService.getUnassignedTrainers("John.Doe")).hasSize(2);
    }

    @Test
    void getUnassignedTrainers_whenTraineeNotFound_throwsAndDoesNotQuery() {
        when(traineeRepository.existsByUsername("Ghost")).thenReturn(false);

        assertThatThrownBy(() -> traineeService.getUnassignedTrainers("Ghost"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainee not found: Ghost");
        verify(traineeRepository, never()).findUnassignedTrainers(anyString());
    }

    // ~~~~~ updateTrainers ~~~~~

    @Test
    void updateTrainers_resolvesEach_setsOnTrainee_andReturnsSet() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));

        Trainer t1 = new Trainer("Ann", "Lee", new TrainingType("Yoga"));
        t1.setUsername("Ann.Lee");
        Trainer t2 = new Trainer("Bob", "Fox", new TrainingType("Fitness"));
        t2.setUsername("Bob.Fox");
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(t1));
        when(trainerRepository.findByUsername("Bob.Fox")).thenReturn(Optional.of(t2));

        Set<Trainer> result = traineeService.updateTrainers("John.Doe", List.of("Ann.Lee", "Bob.Fox"));

        assertThat(result).containsExactlyInAnyOrder(t1, t2);
        assertThat(existingTrainee.getTrainers()).containsExactlyInAnyOrder(t1, t2);
        verify(traineeRepository).save(existingTrainee);
    }

    @Test
    void updateTrainers_whenTrainerNotFound_throwsAndDoesNotSave() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));
        when(trainerRepository.findByUsername("Ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.updateTrainers("John.Doe", List.of("Ghost")))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainer not found: Ghost");
        verify(traineeRepository, never()).save(any());
    }

    @Test
    void updateTrainers_whenTraineeNotFound_throws() {
        when(traineeRepository.findByUsername("Ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.updateTrainers("Ghost", List.of("Ann.Lee")))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainee not found: Ghost");
    }
}
