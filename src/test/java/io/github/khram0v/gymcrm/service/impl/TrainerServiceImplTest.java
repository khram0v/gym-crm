package io.github.khram0v.gymcrm.service.impl;

import io.github.khram0v.gymcrm.exception.AuthenticationException;
import io.github.khram0v.gymcrm.exception.ConflictException;
import io.github.khram0v.gymcrm.exception.NotFoundException;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.model.TrainingType;
import io.github.khram0v.gymcrm.repository.TrainerRepository;
import io.github.khram0v.gymcrm.repository.TrainingTypeRepository;
import io.github.khram0v.gymcrm.service.AuthService;
import io.github.khram0v.gymcrm.util.UserCredentialsGenerator;
import io.github.khram0v.gymcrm.util.UsernameRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @Mock private TrainerRepository trainerRepository;
    @Mock private TrainingTypeRepository trainingTypeRepository;
    @Mock private UserCredentialsGenerator credentialsGenerator;
    @Mock private UsernameRegistry usernameRegistry;
    @Mock private AuthService authService;

    @InjectMocks private TrainerServiceImpl trainerService;

    private TrainingType fitness;
    private Trainer existingTrainer;

    @BeforeEach
    void setUp() {
        fitness = new TrainingType("Fitness");
        existingTrainer = new Trainer("Jane", "Smith", fitness);
        existingTrainer.setUsername("Jane.Smith");
        existingTrainer.setPassword("currentPass");
        existingTrainer.setActive(true);
    }

    // ~~~~~ create ~~~~~

    @Test
    void create_resolvesSpecialization_generatesCredentials_andSaves() {
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(fitness));
        when(credentialsGenerator.generateUsername(anyString(), anyString(), any()))
                .thenReturn("Kate.Novak");
        when(credentialsGenerator.generatePassword()).thenReturn("pass");
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainer result = trainerService.create("Kate", "Novak", 1L);

        assertThat(result.getUsername()).isEqualTo("Kate.Novak");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getSpecialization()).isSameAs(fitness);
        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void create_whenSpecializationNotFound_throwsAndDoesNotSave() {
        when(trainingTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.create("Kate", "Novak", 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Training type not found: 99");
        verify(trainerRepository, never()).save(any());
    }

    // ~~~~~ getByUsername ~~~~~

    @Test
    void getByUsername_whenExists_returns() {
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(existingTrainer));

        assertThat(trainerService.getByUsername("Jane.Smith")).isSameAs(existingTrainer);
    }

    @Test
    void getByUsername_whenNotFound_throws() {
        when(trainerRepository.findByUsername("Ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.getByUsername("Ghost"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainer not found: Ghost");
    }

    // ~~~~~ changePassword ~~~~~

    @Test
    void changePassword_authenticatesOldPassword_thenUpdates() {
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(existingTrainer));

        trainerService.changePassword("Jane.Smith", "currentPass", "newPass");

        verify(authService).authenticate("Jane.Smith", "currentPass");
        assertThat(existingTrainer.getPassword()).isEqualTo("newPass");
        verify(trainerRepository).save(existingTrainer);
    }

    @Test
    void changePassword_whenOldPasswordInvalid_neverUpdates() {
        doThrow(new AuthenticationException("Invalid username or password"))
                .when(authService).authenticate("Jane.Smith", "wrongPass");

        assertThatThrownBy(() -> trainerService.changePassword("Jane.Smith", "wrongPass", "newPass"))
                .isInstanceOf(AuthenticationException.class);
        verify(trainerRepository, never()).findByUsername(anyString());
        verify(trainerRepository, never()).save(any());
    }

    // ~~~~~ updateProfile ~~~~~

    @Test
    void updateProfile_updatesFieldsAndActive_doesNotTouchSpecialization_andSaves() {
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(existingTrainer));
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainer result = trainerService.updateProfile("Jane.Smith", "Janet", "Smith", false);

        assertThat(result.getFirstName()).isEqualTo("Janet");
        assertThat(result.isActive()).isFalse();
        assertThat(result.getSpecialization()).isSameAs(fitness);
        verify(trainerRepository).save(existingTrainer);
        verifyNoInteractions(trainingTypeRepository);
    }

    @Test
    void updateProfile_whenTrainerNotFound_throwsAndDoesNotSave() {
        when(trainerRepository.findByUsername("Ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.updateProfile("Ghost", "A", "B", true))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainer not found: Ghost");
        verify(trainerRepository, never()).save(any());
    }

    // ~~~~~ setActiveStatus ~~~~~

    @Test
    void setActiveStatus_whenChanging_updates() {
        existingTrainer.setActive(false);
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(existingTrainer));

        trainerService.setActiveStatus("Jane.Smith", true);

        assertThat(existingTrainer.isActive()).isTrue();
        verify(trainerRepository).save(existingTrainer);
    }

    @Test
    void setActiveStatus_whenAlreadyInState_throwsAndDoesNotSave() {
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(existingTrainer));

        assertThatThrownBy(() -> trainerService.setActiveStatus("Jane.Smith", true))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already active");
        verify(trainerRepository, never()).save(any());
    }
}
