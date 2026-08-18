package io.github.khram0v.gymcrm.security;

import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.model.Training;
import io.github.khram0v.gymcrm.model.TrainingType;
import io.github.khram0v.gymcrm.repository.TrainingRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceGuardTest {

    @Mock private TrainingRepository trainingRepository;

    private ResourceGuard resourceGuard;

    @BeforeEach
    void setUp() {
        resourceGuard = new ResourceGuard(trainingRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void isTraineeOwner_whenPrincipalIsMatchingTrainee_returnsTrue() {
        authenticateAs("John.Doe", Role.TRAINEE);

        assertThat(resourceGuard.isTraineeOwner("John.Doe")).isTrue();
    }

    @Test
    void isTraineeOwner_whenUsernameDiffers_returnsFalse() {
        authenticateAs("John.Doe", Role.TRAINEE);

        assertThat(resourceGuard.isTraineeOwner("Someone.Else")).isFalse();
    }

    @Test
    void isTraineeOwner_whenPrincipalIsTrainer_returnsFalse() {
        authenticateAs("John.Doe", Role.TRAINER);

        assertThat(resourceGuard.isTraineeOwner("John.Doe")).isFalse();
    }

    @Test
    void isTraineeOwner_whenNoAuthentication_returnsFalse() {
        assertThat(resourceGuard.isTraineeOwner("John.Doe")).isFalse();
    }

    @Test
    void isTraineeOwner_whenPrincipalIsNotUserPrincipal_returnsFalse() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymousUser", null));

        assertThat(resourceGuard.isTraineeOwner("anonymousUser")).isFalse();
    }

    @Test
    void isTrainerOwner_whenPrincipalIsMatchingTrainer_returnsTrue() {
        authenticateAs("Jane.Smith", Role.TRAINER);

        assertThat(resourceGuard.isTrainerOwner("Jane.Smith")).isTrue();
    }

    @Test
    void isTrainerOwner_whenUsernameDiffers_returnsFalse() {
        authenticateAs("Jane.Smith", Role.TRAINER);

        assertThat(resourceGuard.isTrainerOwner("Someone.Else")).isFalse();
    }

    @Test
    void isTrainerOwner_whenPrincipalIsTrainee_returnsFalse() {
        authenticateAs("Jane.Smith", Role.TRAINEE);

        assertThat(resourceGuard.isTrainerOwner("Jane.Smith")).isFalse();
    }

    @Test
    void isTrainingOwner_whenTrainerOwnsTraining_returnsTrue() {
        authenticateAs("Jane.Smith", Role.TRAINER);
        when(trainingRepository.findById(10L)).thenReturn(Optional.of(trainingOwnedBy("Jane.Smith")));

        assertThat(resourceGuard.isTrainingOwner(10L)).isTrue();
    }

    @Test
    void isTrainingOwner_whenTrainerDoesNotOwnTraining_returnsFalse() {
        authenticateAs("Someone.Else", Role.TRAINER);
        when(trainingRepository.findById(10L)).thenReturn(Optional.of(trainingOwnedBy("Jane.Smith")));

        assertThat(resourceGuard.isTrainingOwner(10L)).isFalse();
    }

    @Test
    void isTrainingOwner_whenPrincipalIsTrainee_returnsFalse() {
        authenticateAs("John.Doe", Role.TRAINEE);

        assertThat(resourceGuard.isTrainingOwner(10L)).isFalse();
    }

    @Test
    void isTrainingOwner_whenNoAuthentication_returnsFalse() {
        assertThat(resourceGuard.isTrainingOwner(10L)).isFalse();
    }

    @Test
    void isTrainingOwner_whenTrainingNotFound_returnsTrue_soServiceCanReport404() {
        authenticateAs("Jane.Smith", Role.TRAINER);
        when(trainingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(resourceGuard.isTrainingOwner(99L)).isTrue();
    }

    private Training trainingOwnedBy(String trainerUsername) {
        TrainingType fitness = new TrainingType("Fitness");
        Trainer trainer = new Trainer("Jane", "Smith", fitness);
        trainer.setUsername(trainerUsername);
        Trainee trainee = new Trainee("John", "Doe", null, null);
        trainee.setUsername("John.Doe");
        return new Training(trainee, trainer, "Cardio", fitness, LocalDate.now().plusDays(1), 60);
    }

    private void authenticateAs(String username, Role role) {
        UserPrincipal principal = new UserPrincipal(username, "encodedPass", true, role);
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
