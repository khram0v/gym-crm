package io.github.khram0v.gymcrm.service.impl;

import io.github.khram0v.gymcrm.dto.response.RegistrationResponse;
import io.github.khram0v.gymcrm.dto.response.TraineeProfileResponse;
import io.github.khram0v.gymcrm.dto.response.TrainerSummary;
import io.github.khram0v.gymcrm.exception.AuthenticationException;
import io.github.khram0v.gymcrm.exception.ConflictException;
import io.github.khram0v.gymcrm.exception.NotFoundException;
import io.github.khram0v.gymcrm.mapper.SummaryMapper;
import io.github.khram0v.gymcrm.mapper.TraineeMapper;
import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.model.TrainingType;
import io.github.khram0v.gymcrm.repository.TraineeRepository;
import io.github.khram0v.gymcrm.repository.TrainerRepository;
import io.github.khram0v.gymcrm.security.PasswordVerifier;
import io.github.khram0v.gymcrm.util.UserCredentialsGenerator;
import io.github.khram0v.gymcrm.util.UsernameRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    @Mock private TraineeMapper traineeMapper;
    @Mock private SummaryMapper summaryMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PasswordVerifier passwordVerifier;
    @Spy private MeterRegistry meterRegistry = new SimpleMeterRegistry();

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
    void create_generatesCredentials_encodesPassword_activates_andReturnsRawPasswordInResponse() {
        when(credentialsGenerator.generateUsername(eq("Alan"), eq("Poe"), any()))
                .thenReturn("Alan.Poe");
        when(credentialsGenerator.generatePassword()).thenReturn("pass123");
        when(passwordEncoder.encode("pass123")).thenReturn("encodedPass123");
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        RegistrationResponse result = traineeService.create("Alan", "Poe",
                LocalDate.of(2000, Month.JANUARY, 1), "Main St");

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeRepository).save(captor.capture());
        Trainee saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("Alan.Poe");
        assertThat(saved.getPassword()).isEqualTo("encodedPass123");
        assertThat(saved.isActive()).isTrue();

        assertThat(result).isEqualTo(new RegistrationResponse("Alan.Poe", "pass123"));

        assertThat(meterRegistry.get("gym.trainee.registrations").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("gym.trainee.registration.duration").timer().count()).isEqualTo(1);
    }

    // ~~~~~ getByUsername ~~~~~

    @Test
    void getByUsername_whenExists_mapsAndReturns() {
        TraineeProfileResponse stub = sampleProfile();
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));
        when(traineeMapper.toProfileResponse(existingTrainee)).thenReturn(stub);

        TraineeProfileResponse result = traineeService.getByUsername("John.Doe");

        assertThat(result).isSameAs(stub);
        verify(traineeMapper).toProfileResponse(existingTrainee);
    }

    @Test
    void getByUsername_whenNotFound_throwsAndDoesNotMap() {
        when(traineeRepository.findByUsername("Ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.getByUsername("Ghost"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainee not found: Ghost");
        verify(traineeMapper, never()).toProfileResponse(any());
    }

    // ~~~~~ changePassword ~~~~~

    @Test
    void changePassword_verifiesOldPassword_setsEncodedNewPassword_andSaves() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        traineeService.changePassword("John.Doe", "currentPass", "newPass");

        verify(passwordVerifier).verify("currentPass", "currentPass");
        assertThat(existingTrainee.getPassword()).isEqualTo("encodedNewPass");
        verify(traineeRepository).save(existingTrainee);
    }

    @Test
    void changePassword_whenOldPasswordVerificationFails_propagatesAndDoesNotSave() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));
        doThrow(new AuthenticationException("Invalid username or password"))
                .when(passwordVerifier).verify("wrongPass", "currentPass");

        assertThatThrownBy(() -> traineeService.changePassword("John.Doe", "wrongPass", "newPass"))
                .isInstanceOf(AuthenticationException.class);
        verify(traineeRepository, never()).save(any());
    }

    // ~~~~~ updateProfile ~~~~~

    @Test
    void updateProfile_updatesFieldsIncludingActive_savesAndMaps() {
        TraineeProfileResponse stub = sampleProfile();
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));
        when(traineeMapper.toProfileResponse(any(Trainee.class))).thenReturn(stub);

        TraineeProfileResponse result = traineeService.updateProfile(
                "John.Doe", "Johnny", "Doe", LocalDate.of(1999, Month.MAY, 5), "New Address", false);

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeMapper).toProfileResponse(captor.capture());
        Trainee mapped = captor.getValue();
        assertThat(mapped.getFirstName()).isEqualTo("Johnny");
        assertThat(mapped.getDateOfBirth()).isEqualTo(LocalDate.of(1999, Month.MAY, 5));
        assertThat(mapped.getAddress()).isEqualTo("New Address");
        assertThat(mapped.isActive()).isFalse();

        assertThat(result).isSameAs(stub);
        verify(traineeRepository).save(existingTrainee);
    }

    @Test
    void updateProfile_whenNotFound_throwsAndDoesNotSave() {
        when(traineeRepository.findByUsername("Ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.updateProfile("Ghost", "A", "B", null, null, true))
                .isInstanceOf(NotFoundException.class);
        verify(traineeRepository, never()).save(any());
        verify(traineeMapper, never()).toProfileResponse(any());
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
    void getUnassignedTrainers_whenTraineeExists_queriesAndMapsSorted() {
        Trainer t1 = new Trainer("Ann", "Lee", new TrainingType("Yoga"));
        Trainer t2 = new Trainer("Bob", "Fox", new TrainingType("Fitness"));
        Set<Trainer> unassigned = Set.of(t1, t2);
        List<TrainerSummary> stub = List.of(sampleSummary(), sampleSummary());

        when(traineeRepository.existsByUsername("John.Doe")).thenReturn(true);
        when(traineeRepository.findUnassignedTrainers("John.Doe")).thenReturn(unassigned);
        when(summaryMapper.trainerSetToSortedSummaries(unassigned)).thenReturn(stub);

        List<TrainerSummary> result = traineeService.getUnassignedTrainers("John.Doe");

        assertThat(result).isSameAs(stub);
        verify(summaryMapper).trainerSetToSortedSummaries(unassigned);
    }

    @Test
    void getUnassignedTrainers_whenTraineeNotFound_throwsAndDoesNotQuery() {
        when(traineeRepository.existsByUsername("Ghost")).thenReturn(false);

        assertThatThrownBy(() -> traineeService.getUnassignedTrainers("Ghost"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainee not found: Ghost");
        verify(traineeRepository, never()).findUnassignedTrainers(anyString());
        verify(summaryMapper, never()).trainerSetToSortedSummaries(any());
    }

    // ~~~~~ updateTrainers ~~~~~

    @Test
    void updateTrainers_resolvesEach_setsOnTrainee_savesAndMaps() {
        Trainer t1 = new Trainer("Ann", "Lee", new TrainingType("Yoga"));
        t1.setUsername("Ann.Lee");
        Trainer t2 = new Trainer("Bob", "Fox", new TrainingType("Fitness"));
        t2.setUsername("Bob.Fox");
        List<TrainerSummary> stub = List.of(sampleSummary(), sampleSummary());

        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(t1));
        when(trainerRepository.findByUsername("Bob.Fox")).thenReturn(Optional.of(t2));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));
        when(summaryMapper.trainerSetToSortedSummaries(any())).thenReturn(stub);

        List<TrainerSummary> result = traineeService.updateTrainers(
                "John.Doe", List.of("Ann.Lee", "Bob.Fox"));

        assertThat(existingTrainee.getTrainers()).containsExactlyInAnyOrder(t1, t2);
        assertThat(result).isSameAs(stub);
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

    // ~~~~~ helpers ~~~~~

    private TraineeProfileResponse sampleProfile() {
        return new TraineeProfileResponse("John.Doe", "John", "Doe",
                LocalDate.of(1995, Month.MAY, 20), "123 Main St", true, List.of());
    }

    private TrainerSummary sampleSummary() {
        return new TrainerSummary("Jane.Smith", "Jane", "Smith", null);
    }
}
