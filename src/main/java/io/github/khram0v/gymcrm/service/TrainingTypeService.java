package io.github.khram0v.gymcrm.service;

import io.github.khram0v.gymcrm.model.TrainingType;

import java.util.List;

public interface TrainingTypeService {

    List<TrainingType> getAll();

    TrainingType getById(Long id);
}
