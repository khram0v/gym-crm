package io.github.khram0v.gymcrm.controller;

import io.github.khram0v.gymcrm.api.TrainingTypeApi;
import io.github.khram0v.gymcrm.dto.response.TrainingTypeResponse;
import io.github.khram0v.gymcrm.service.TrainingTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TrainingTypeController implements TrainingTypeApi {

    private final TrainingTypeService trainingTypeService;

    @Override
    public List<TrainingTypeResponse> getAll() {
        return trainingTypeService.getAll();
    }
}
