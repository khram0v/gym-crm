package io.github.khram0v.gymcrm.service;

import io.github.khram0v.gymcrm.repository.TraineeRepository;
import io.github.khram0v.gymcrm.repository.TrainerRepository;
import io.github.khram0v.gymcrm.exception.AuthenticationException;
import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.model.TrainingType;
import io.github.khram0v.gymcrm.util.UserCredentialsGenerator;
import io.github.khram0v.gymcrm.util.UsernameRegistry;
import io.github.khram0v.gymcrm.validation.EntityValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock private TraineeRepository traineeRepository;
    @Mock private TrainerRepository trainerRepository;
    @Mock private AuthenticationService authenticationService;
    @Mock private UserCredentialsGenerator credentialsGenerator;
    @Mock private UsernameRegistry usernameRegistry;
    @Mock private EntityValidator validator;

    @InjectMocks private TraineeService traineeService;

    private Trainee existingTrainee;

    @BeforeEach
    void setUp() {
        existingTrainee = new Trainee("John", "Doe",
                LocalDate.of(1995, Month.MAY, 20), "123 Main St");
        existingTrainee.setUsername("John.Doe");
        existingTrainee.setPassword("currentPass");
        existingTrainee.setActive(true);
    }

    // ~~~~~ Create ~~~~~

    @Test
    void create_generatesCredentials_setsActive_andSaves() {
        when(credentialsGenerator.generateUsername(eq("Jane"), eq("Roe"), any()))
                .thenReturn("Jane.Roe");
        when(credentialsGenerator.generatePassword()).thenReturn("genPass123");
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainee result = traineeService.create("Jane", "Roe",
                LocalDate.of(2000, Month.JANUARY, 1), "456 Oak Ave");

        assertThat(result.getUsername()).isEqualTo("Jane.Roe");
        assertThat(result.getPassword()).isEqualTo("genPass123");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Roe");
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(2000, Month.JANUARY, 1));
        assertThat(result.getAddress()).isEqualTo("456 Oak Ave");
    }

    @Test
    void create_validatesBeforeSaving() {
        when(credentialsGenerator.generateUsername(any(), any(), any())).thenReturn("Jane.Roe");
        when(credentialsGenerator.generatePassword()).thenReturn("genPass123");
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        traineeService.create("Jane", "Roe", LocalDate.of(2000, Month.JANUARY, 1), "456 Oak Ave");

        InOrder order = inOrder(validator, traineeRepository);
        order.verify(validator).validate(any(Trainee.class));
        order.verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void create_doesNotAuthenticate() {
        when(credentialsGenerator.generateUsername(any(), any(), any())).thenReturn("Jane.Roe");
        when(credentialsGenerator.generatePassword()).thenReturn("genPass123");
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        traineeService.create("Jane", "Roe", LocalDate.of(2000, Month.JANUARY, 1), "456 Oak Ave");

        verify(authenticationService, never()).authenticateTrainee(any(), any());
    }

    // ~~~~~ Get by username ~~~~~

    @Test
    void getByUsername_authenticatesThenReturns() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));

        Trainee result = traineeService.getByUsername("John.Doe", "currentPass");

        assertThat(result).isSameAs(existingTrainee);
        verify(authenticationService).authenticateTrainee("John.Doe", "currentPass");
    }

    @Test
    void getByUsername_whenAuthFails_neverQueriesRepository() {
        doThrow(new AuthenticationException("Invalid username or password"))
                .when(authenticationService).authenticateTrainee("John.Doe", "wrong");

        assertThatThrownBy(() -> traineeService.getByUsername("John.Doe", "wrong"))
                .isInstanceOf(AuthenticationException.class);

        verify(traineeRepository, never()).findByUsername(any());
    }

    @Test
    void getByUsername_whenNotFound_throws() {
        when(traineeRepository.findByUsername("Ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.getByUsername("Ghost", "pass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ghost");
    }

    // ~~~~~ Change password ~~~~~

    @Test
    void changePassword_authenticatesWithOldPassword_thenUpdates() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));

        traineeService.changePassword("John.Doe", "currentPass", "newPass456");

        assertThat(existingTrainee.getPassword()).isEqualTo("newPass456");
        verify(authenticationService).authenticateTrainee("John.Doe", "currentPass");
        verify(validator).validate(existingTrainee);
        verify(traineeRepository).save(existingTrainee);
    }

    @Test
    void changePassword_whenAuthFails_neverUpdates() {
        doThrow(new AuthenticationException("Invalid username or password"))
                .when(authenticationService).authenticateTrainee("John.Doe", "wrong");

        assertThatThrownBy(() ->
                traineeService.changePassword("John.Doe", "wrong", "newPass456"))
                .isInstanceOf(AuthenticationException.class);

        verify(traineeRepository, never()).save(any());
    }

    // ~~~~~ Update profile ~~~~~

    @Test
    void updateProfile_authenticates_updatesFields_validates_andPersists() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainee result = traineeService.updateProfile("John.Doe", "currentPass",
                "Johnny", "Doer", LocalDate.of(1990, Month.MARCH, 3), "789 Pine Rd");

        assertThat(result.getFirstName()).isEqualTo("Johnny");
        assertThat(result.getLastName()).isEqualTo("Doer");
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1990, Month.MARCH, 3));
        assertThat(result.getAddress()).isEqualTo("789 Pine Rd");

        verify(authenticationService).authenticateTrainee("John.Doe", "currentPass");

        InOrder order = inOrder(validator, traineeRepository);
        order.verify(validator).validate(existingTrainee);
        order.verify(traineeRepository).save(existingTrainee);
    }

    @Test
    void updateProfile_whenAuthFails_neverPersists() {
        doThrow(new AuthenticationException("Invalid username or password"))
                .when(authenticationService).authenticateTrainee("John.Doe", "wrong");

        assertThatThrownBy(() -> traineeService.updateProfile("John.Doe", "wrong",
                "Johnny", "Doer", LocalDate.of(1990, Month.MARCH, 3), "789 Pine Rd"))
                .isInstanceOf(AuthenticationException.class);

        verify(traineeRepository, never()).save(any());
    }

    // ~~~~~ Activate/deactivate ~~~~~

    @Test
    void setActiveStatus_whenChangingFromActiveToInactive_updates() {
        existingTrainee.setActive(true);
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));

        traineeService.setActiveStatus("John.Doe", "currentPass", false);

        assertThat(existingTrainee.isActive()).isFalse();
        verify(traineeRepository).save(existingTrainee);
    }

    @Test
    void setActiveStatus_whenChangingFromInactiveToActive_updates() {
        existingTrainee.setActive(false);
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));

        traineeService.setActiveStatus("John.Doe", "currentPass", true);

        assertThat(existingTrainee.isActive()).isTrue();
        verify(traineeRepository).save(existingTrainee);
    }

    @Test
    void setActiveStatus_whenAlreadyActive_throwsAndDoesNotSave() {
        existingTrainee.setActive(true);
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));

        assertThatThrownBy(() ->
                traineeService.setActiveStatus("John.Doe", "currentPass", true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already active");

        verify(traineeRepository, never()).save(any());
    }

    @Test
    void setActiveStatus_whenAlreadyInactive_throwsAndDoesNotSave() {
        existingTrainee.setActive(false);
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));

        assertThatThrownBy(() ->
                traineeService.setActiveStatus("John.Doe", "currentPass", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already inactive");

        verify(traineeRepository, never()).save(any());
    }

    // ~~~~~ Delete ~~~~~

    @Test
    void deleteByUsername_authenticates_thenDeletes() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));

        traineeService.deleteByUsername("John.Doe", "currentPass");

        verify(authenticationService).authenticateTrainee("John.Doe", "currentPass");
        verify(traineeRepository).delete(existingTrainee);
    }

    @Test
    void deleteByUsername_whenAuthFails_neverDeletes() {
        doThrow(new AuthenticationException("Invalid username or password"))
                .when(authenticationService).authenticateTrainee("John.Doe", "wrong");

        assertThatThrownBy(() -> traineeService.deleteByUsername("John.Doe", "wrong"))
                .isInstanceOf(AuthenticationException.class);

        verify(traineeRepository, never()).delete(any());
    }

    @Test
    void deleteByUsername_whenNotFound_throws() {
        when(traineeRepository.findByUsername("Ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.deleteByUsername("Ghost", "pass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ghost");

        verify(traineeRepository, never()).delete(any());
    }

    // ~~~~~ Unassigned trainers ~~~~~

    @Test
    void getUnassignedTrainers_authenticates_thenReturnsList() {
        Trainer t1 = new Trainer("Jane", "Smith", new TrainingType("Fitness"));
        Trainer t2 = new Trainer("Bob", "Jones", new TrainingType("Yoga"));
        when(traineeRepository.findUnassignedTrainers("John.Doe")).thenReturn(List.of(t1, t2));

        List<Trainer> result = traineeService.getUnassignedTrainers("John.Doe", "currentPass");

        assertThat(result).containsExactly(t1, t2);
        verify(authenticationService).authenticateTrainee("John.Doe", "currentPass");
    }

    @Test
    void getUnassignedTrainers_whenAuthFails_neverQueries() {
        doThrow(new AuthenticationException("Invalid username or password"))
                .when(authenticationService).authenticateTrainee("John.Doe", "wrong");

        assertThatThrownBy(() ->
                traineeService.getUnassignedTrainers("John.Doe", "wrong"))
                .isInstanceOf(AuthenticationException.class);

        verify(traineeRepository, never()).findUnassignedTrainers(any());
    }

    // ~~~~~ Update trainers list ~~~~~

    @Test
    void updateTrainers_authenticates_resolvesTrainers_andPersists() {
        Trainer t1 = new Trainer("Jane", "Smith", new TrainingType("Fitness"));
        t1.setUsername("Jane.Smith");
        Trainer t2 = new Trainer("Bob", "Jones", new TrainingType("Yoga"));
        t2.setUsername("Bob.Jones");

        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(t1));
        when(trainerRepository.findByUsername("Bob.Jones")).thenReturn(Optional.of(t2));

        List<Trainer> result = traineeService.updateTrainers("John.Doe", "currentPass",
                List.of("Jane.Smith", "Bob.Jones"));

        assertThat(result).containsExactly(t1, t2);
        assertThat(existingTrainee.getTrainers()).containsExactly(t1, t2);
        verify(authenticationService).authenticateTrainee("John.Doe", "currentPass");
        verify(traineeRepository).save(existingTrainee);
    }

    @Test
    void updateTrainers_whenATrainerNotFound_throwsAndDoesNotPersist() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));
        when(trainerRepository.findByUsername("Ghost.Trainer")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.updateTrainers("John.Doe", "currentPass",
                List.of("Ghost.Trainer")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ghost.Trainer");

        verify(traineeRepository, never()).save(any());
    }

    @Test
    void updateTrainers_whenAuthFails_neverResolvesOrPersists() {
        doThrow(new AuthenticationException("Invalid username or password"))
                .when(authenticationService).authenticateTrainee("John.Doe", "wrong");

        assertThatThrownBy(() -> traineeService.updateTrainers("John.Doe", "wrong",
                List.of("Jane.Smith")))
                .isInstanceOf(AuthenticationException.class);

        verify(trainerRepository, never()).findByUsername(any());
        verify(traineeRepository, never()).save(any());
    }
}
