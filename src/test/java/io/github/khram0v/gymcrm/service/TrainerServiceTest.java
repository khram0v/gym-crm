package io.github.khram0v.gymcrm.service;

import io.github.khram0v.gymcrm.repository.TrainerRepository;
import io.github.khram0v.gymcrm.repository.TrainingTypeRepository;
import io.github.khram0v.gymcrm.exception.AuthenticationException;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock private TrainerRepository trainerRepository;
    @Mock private TrainingTypeRepository trainingTypeRepository;
    @Mock private AuthenticationService authenticationService;
    @Mock private UserCredentialsGenerator credentialsGenerator;
    @Mock private UsernameRegistry usernameRegistry;
    @Mock private EntityValidator validator;

    @InjectMocks private TrainerService trainerService;

    private Trainer existingTrainer;
    private TrainingType fitness;

    @BeforeEach
    void setUp() {
        fitness = new TrainingType("Fitness");
        existingTrainer = new Trainer("Jane", "Smith", fitness);
        existingTrainer.setUsername("Jane.Smith");
        existingTrainer.setPassword("currentPass");
        existingTrainer.setActive(true);
    }

    // ~~~~~ Create ~~~~~

    @Test
    void create_resolvesSpecialization_generatesCredentials_setsActive_andSaves() {
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(fitness));
        when(credentialsGenerator.generateUsername(eq("Bob"), eq("Jones"), any()))
                .thenReturn("Bob.Jones");
        when(credentialsGenerator.generatePassword()).thenReturn("genPass123");
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainer result = trainerService.create("Bob", "Jones", 1L);

        assertThat(result.getUsername()).isEqualTo("Bob.Jones");
        assertThat(result.getPassword()).isEqualTo("genPass123");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getFirstName()).isEqualTo("Bob");
        assertThat(result.getLastName()).isEqualTo("Jones");
        assertThat(result.getSpecialization()).isSameAs(fitness);
    }

    @Test
    void create_validatesBeforeSaving() {
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(fitness));
        when(credentialsGenerator.generateUsername(any(), any(), any())).thenReturn("Bob.Jones");
        when(credentialsGenerator.generatePassword()).thenReturn("genPass123");
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        trainerService.create("Bob", "Jones", 1L);

        InOrder order = inOrder(validator, trainerRepository);
        order.verify(validator).validate(any(Trainer.class));
        order.verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void create_whenSpecializationNotFound_throwsAndDoesNotSave() {
        when(trainingTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.create("Bob", "Jones", 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void create_doesNotAuthenticate() {
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(fitness));
        when(credentialsGenerator.generateUsername(any(), any(), any())).thenReturn("Bob.Jones");
        when(credentialsGenerator.generatePassword()).thenReturn("genPass123");
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        trainerService.create("Bob", "Jones", 1L);

        verify(authenticationService, never()).authenticateTrainer(any(), any());
    }

    // ~~~~~ Get by username ~~~~~

    @Test
    void getByUsername_authenticatesThenReturns() {
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(existingTrainer));

        Trainer result = trainerService.getByUsername("Jane.Smith", "currentPass");

        assertThat(result).isSameAs(existingTrainer);
        verify(authenticationService).authenticateTrainer("Jane.Smith", "currentPass");
    }

    @Test
    void getByUsername_whenAuthFails_neverQueriesRepository() {
        doThrow(new AuthenticationException("Invalid username or password"))
                .when(authenticationService).authenticateTrainer("Jane.Smith", "wrong");

        assertThatThrownBy(() -> trainerService.getByUsername("Jane.Smith", "wrong"))
                .isInstanceOf(AuthenticationException.class);

        verify(trainerRepository, never()).findByUsername(any());
    }

    @Test
    void getByUsername_whenNotFound_throws() {
        when(trainerRepository.findByUsername("Ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.getByUsername("Ghost", "pass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ghost");
    }

    // ~~~~~ Change password ~~~~~

    @Test
    void changePassword_authenticatesWithOldPassword_thenUpdates() {
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(existingTrainer));

        trainerService.changePassword("Jane.Smith", "currentPass", "newPass456");

        assertThat(existingTrainer.getPassword()).isEqualTo("newPass456");
        verify(authenticationService).authenticateTrainer("Jane.Smith", "currentPass");
        verify(validator).validate(existingTrainer);
        verify(trainerRepository).save(existingTrainer);
    }

    @Test
    void changePassword_whenAuthFails_neverUpdates() {
        doThrow(new AuthenticationException("Invalid username or password"))
                .when(authenticationService).authenticateTrainer("Jane.Smith", "wrong");

        assertThatThrownBy(() ->
                trainerService.changePassword("Jane.Smith", "wrong", "newPass456"))
                .isInstanceOf(AuthenticationException.class);

        verify(trainerRepository, never()).save(any());
    }

    // ~~~~~ Update profile ~~~~~

    @Test
    void updateProfile_authenticates_resolvesSpecialization_updatesFields_validates_andPersists() {
        TrainingType yoga = new TrainingType("Yoga");
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(existingTrainer));
        when(trainingTypeRepository.findById(2L)).thenReturn(Optional.of(yoga));
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainer result = trainerService.updateProfile("Jane.Smith", "currentPass",
                "Janet", "Smithers", 2L);

        assertThat(result.getFirstName()).isEqualTo("Janet");
        assertThat(result.getLastName()).isEqualTo("Smithers");
        assertThat(result.getSpecialization()).isSameAs(yoga);

        verify(authenticationService).authenticateTrainer("Jane.Smith", "currentPass");

        InOrder order = inOrder(validator, trainerRepository);
        order.verify(validator).validate(existingTrainer);
        order.verify(trainerRepository).save(existingTrainer);
    }

    @Test
    void updateProfile_whenSpecializationNotFound_throwsAndDoesNotPersist() {
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(existingTrainer));
        when(trainingTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.updateProfile("Jane.Smith", "currentPass",
                "Janet", "Smithers", 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void updateProfile_whenAuthFails_neverPersists() {
        doThrow(new AuthenticationException("Invalid username or password"))
                .when(authenticationService).authenticateTrainer("Jane.Smith", "wrong");

        assertThatThrownBy(() -> trainerService.updateProfile("Jane.Smith", "wrong",
                "Janet", "Smithers", 2L))
                .isInstanceOf(AuthenticationException.class);

        verify(trainerRepository, never()).save(any());
    }

    // ~~~~~ Activate/deactivate ~~~~~

    @Test
    void setActiveStatus_whenChangingFromActiveToInactive_updates() {
        existingTrainer.setActive(true);
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(existingTrainer));

        trainerService.setActiveStatus("Jane.Smith", "currentPass", false);

        assertThat(existingTrainer.isActive()).isFalse();
        verify(trainerRepository).save(existingTrainer);
    }

    @Test
    void setActiveStatus_whenChangingFromInactiveToActive_updates() {
        existingTrainer.setActive(false);
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(existingTrainer));

        trainerService.setActiveStatus("Jane.Smith", "currentPass", true);

        assertThat(existingTrainer.isActive()).isTrue();
        verify(trainerRepository).save(existingTrainer);
    }

    @Test
    void setActiveStatus_whenAlreadyActive_throwsAndDoesNotSave() {
        existingTrainer.setActive(true);
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(existingTrainer));

        assertThatThrownBy(() ->
                trainerService.setActiveStatus("Jane.Smith", "currentPass", true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already active");

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void setActiveStatus_whenAlreadyInactive_throwsAndDoesNotSave() {
        existingTrainer.setActive(false);
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(existingTrainer));

        assertThatThrownBy(() ->
                trainerService.setActiveStatus("Jane.Smith", "currentPass", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already inactive");

        verify(trainerRepository, never()).save(any());
    }
}
