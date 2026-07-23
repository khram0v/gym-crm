package io.github.khram0v.gymcrm.service.impl;

import io.github.khram0v.gymcrm.model.TrainingType;
import io.github.khram0v.gymcrm.repository.TrainingTypeRepository;
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
class TrainingTypeServiceImplTest {

    @Mock private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks private TrainingTypeServiceImpl trainingTypeService;

    @Test
    void getAll_returnsAllTypes() {
        when(trainingTypeRepository.findAll())
                .thenReturn(List.of(new TrainingType("Fitness"), new TrainingType("Yoga")));

        assertThat(trainingTypeService.getAll()).hasSize(2);
    }

    @Test
    void getById_whenExists_returnsType() {
        TrainingType type = new TrainingType("Fitness");
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(type));

        assertThat(trainingTypeService.getById(1L)).isSameAs(type);
    }

    @Test
    void getById_whenNotFound_throws() {
        when(trainingTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingTypeService.getById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Training type not found: 99");
    }
}
