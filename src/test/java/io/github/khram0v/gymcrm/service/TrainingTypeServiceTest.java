package io.github.khram0v.gymcrm.service;

import io.github.khram0v.gymcrm.repository.TrainingTypeRepository;
import io.github.khram0v.gymcrm.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceTest {

    @Mock private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks private TrainingTypeService trainingTypeService;

    @Test
    void getAll_returnsAllTypesFromRepository() {
        TrainingType fitness = new TrainingType("Fitness");
        TrainingType yoga = new TrainingType("Yoga");
        when(trainingTypeRepository.findAll()).thenReturn(List.of(fitness, yoga));

        List<TrainingType> result = trainingTypeService.getAll();

        assertThat(result).containsExactly(fitness, yoga);
    }

    @Test
    void getAll_whenNoneExist_returnsEmptyList() {
        when(trainingTypeRepository.findAll()).thenReturn(List.of());

        assertThat(trainingTypeService.getAll()).isEmpty();
    }

    @Test
    void getById_whenFound_returnsType() {
        TrainingType fitness = new TrainingType("Fitness");
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(fitness));

        assertThat(trainingTypeService.getById(1L)).isSameAs(fitness);
    }

    @Test
    void getById_whenNotFound_throws() {
        when(trainingTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingTypeService.getById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }
}
