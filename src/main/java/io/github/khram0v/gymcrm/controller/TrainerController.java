package io.github.khram0v.gymcrm.controller;

import io.github.khram0v.gymcrm.api.TrainerApi;
import io.github.khram0v.gymcrm.dto.request.ActivateRequest;
import io.github.khram0v.gymcrm.dto.request.ChangePasswordRequest;
import io.github.khram0v.gymcrm.dto.request.TrainerRegistrationRequest;
import io.github.khram0v.gymcrm.dto.request.UpdateTrainerRequest;
import io.github.khram0v.gymcrm.dto.response.RegistrationResponse;
import io.github.khram0v.gymcrm.dto.response.TrainerProfileResponse;
import io.github.khram0v.gymcrm.dto.response.TrainerTrainingResponse;
import io.github.khram0v.gymcrm.service.TrainerService;
import io.github.khram0v.gymcrm.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TrainerController implements TrainerApi {

    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @Override
    public RegistrationResponse register(TrainerRegistrationRequest request) {
        return trainerService.create(
                request.firstName(), request.lastName(), request.specializationId());
    }

    @Override
    public TrainerProfileResponse getProfile(String username) {
        return trainerService.getByUsername(username);
    }

    @Override
    public void changePassword(String username, ChangePasswordRequest request) {
        trainerService.changePassword(username, request.oldPassword(), request.newPassword());
    }

    @Override
    public TrainerProfileResponse updateProfile(String username, UpdateTrainerRequest request) {
        return trainerService.updateProfile(
                username, request.firstName(), request.lastName(), request.active());
    }

    @Override
    public List<TrainerTrainingResponse> getTrainings(String username,
                                                      LocalDate from,
                                                      LocalDate to,
                                                      String traineeFirstName,
                                                      String traineeLastName) {
        return trainingService.getTrainerTrainings(username, from, to, traineeFirstName, traineeLastName);
    }

    @Override
    public void setActiveStatus(String username, ActivateRequest request) {
        trainerService.setActiveStatus(username, request.active());
    }
}
