package io.github.khram0v.gymcrm.controller;

import io.github.khram0v.gymcrm.api.TrainingApi;
import io.github.khram0v.gymcrm.dto.request.AddTrainingRequest;
import io.github.khram0v.gymcrm.dto.response.TrainingTypeResponse;
import io.github.khram0v.gymcrm.service.TrainingService;
import io.github.khram0v.gymcrm.service.TrainingTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trainings")
@RequiredArgsConstructor
public class TrainingController implements TrainingApi {

    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addTraining(@RequestBody AddTrainingRequest request) {
        trainingService.addTraining(
                request.trainerUsername(), request.traineeUsername(),
                request.trainingName(), request.trainingDate(), request.trainingDuration());
    }

    @Override
    @GetMapping("/training-types")
    public List<TrainingTypeResponse> getAllTrainingTypes() {
        return trainingTypeService.getAll();
    }
}
