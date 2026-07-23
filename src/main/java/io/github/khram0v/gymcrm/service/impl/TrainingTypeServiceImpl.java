package io.github.khram0v.gymcrm.service.impl;

import io.github.khram0v.gymcrm.repository.TrainingTypeRepository;
import io.github.khram0v.gymcrm.model.TrainingType;
import io.github.khram0v.gymcrm.service.TrainingTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingTypeServiceImpl implements TrainingTypeService {

    private final TrainingTypeRepository trainingTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TrainingType> getAll() {
        return trainingTypeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public TrainingType getById(Long id) {
        return trainingTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Training type not found: " + id));
    }
}
