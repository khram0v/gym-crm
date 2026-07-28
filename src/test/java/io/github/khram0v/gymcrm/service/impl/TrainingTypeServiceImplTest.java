package io.github.khram0v.gymcrm.service.impl;

import io.github.khram0v.gymcrm.dto.response.TrainingTypeResponse;
import io.github.khram0v.gymcrm.mapper.TrainingTypeMapper;
import io.github.khram0v.gymcrm.model.TrainingType;
import io.github.khram0v.gymcrm.repository.TrainingTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceImplTest {

    @Mock private TrainingTypeRepository trainingTypeRepository;
    @Mock private TrainingTypeMapper trainingTypeMapper;

    @InjectMocks private TrainingTypeServiceImpl trainingTypeService;

    @Test
    void getAll_queriesAndMaps() {
        List<TrainingType> entities = List.of(new TrainingType("Fitness"), new TrainingType("Yoga"));
        List<TrainingTypeResponse> stub = List.of(
                new TrainingTypeResponse(1L, "Fitness"), new TrainingTypeResponse(2L, "Yoga"));
        when(trainingTypeRepository.findAll()).thenReturn(entities);
        when(trainingTypeMapper.toResponseList(entities)).thenReturn(stub);

        List<TrainingTypeResponse> result = trainingTypeService.getAll();

        assertThat(result).isSameAs(stub);
        verify(trainingTypeMapper).toResponseList(entities);
    }
}
