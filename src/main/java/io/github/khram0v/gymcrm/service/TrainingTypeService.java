package io.github.khram0v.gymcrm.service;

import io.github.khram0v.gymcrm.dto.response.TrainingTypeResponse;

import java.util.List;

public interface TrainingTypeService {

    List<TrainingTypeResponse> getAll();
}
