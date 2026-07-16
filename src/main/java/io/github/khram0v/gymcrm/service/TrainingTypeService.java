package io.github.khram0v.gymcrm.service;

import io.github.khram0v.gymcrm.repository.TrainingTypeRepository;
import io.github.khram0v.gymcrm.model.TrainingType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainingTypeService {

    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypeService(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<TrainingType> getAll() {
        return trainingTypeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public TrainingType getById(Long id) {
        return trainingTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Training type not found: id=" + id));
    }
}
