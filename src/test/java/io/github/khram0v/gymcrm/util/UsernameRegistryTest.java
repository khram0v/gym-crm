package io.github.khram0v.gymcrm.util;

import io.github.khram0v.gymcrm.repository.TraineeRepository;
import io.github.khram0v.gymcrm.repository.TrainerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsernameRegistryTest {

    @Mock private TraineeRepository traineeRepository;
    @Mock private TrainerRepository trainerRepository;

    @InjectMocks private UsernameRegistry usernameRegistry;

    @Test
    void exists_whenTraineeHasUsername_returnsTrue() {
        when(traineeRepository.existsByUsername("John.Doe")).thenReturn(true);

        assertThat(usernameRegistry.exists("John.Doe")).isTrue();

        verify(trainerRepository, never()).existsByUsername("John.Doe");
    }

    @Test
    void exists_whenOnlyTrainerHasUsername_returnsTrue() {
        when(traineeRepository.existsByUsername("Jane.Smith")).thenReturn(false);
        when(trainerRepository.existsByUsername("Jane.Smith")).thenReturn(true);

        assertThat(usernameRegistry.exists("Jane.Smith")).isTrue();

        verify(traineeRepository).existsByUsername("Jane.Smith");
        verify(trainerRepository).existsByUsername("Jane.Smith");
    }

    @Test
    void exists_whenNeitherPoolHasUsername_returnsFalse() {
        when(traineeRepository.existsByUsername("New.User")).thenReturn(false);
        when(trainerRepository.existsByUsername("New.User")).thenReturn(false);

        assertThat(usernameRegistry.exists("New.User")).isFalse();

        verify(traineeRepository).existsByUsername("New.User");
        verify(trainerRepository).existsByUsername("New.User");
    }
}
