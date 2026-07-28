package io.github.khram0v.gymcrm.controller;

import io.github.khram0v.gymcrm.api.TrainingApi;
import io.github.khram0v.gymcrm.dto.request.AddTrainingRequest;
import io.github.khram0v.gymcrm.dto.response.TrainingTypeResponse;
import io.github.khram0v.gymcrm.service.TrainingService;
import io.github.khram0v.gymcrm.service.TrainingTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TrainingController implements TrainingApi {

    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;

    @Override
    public void addTraining(AddTrainingRequest request) {
        trainingService.addTraining(
                request.trainerUsername(), request.traineeUsername(),
                request.trainingName(), request.trainingDate(), request.trainingDuration());
    }

    @Override
    public List<TrainingTypeResponse> getAllTrainingTypes() {
        return trainingTypeService.getAll();
    }
}
