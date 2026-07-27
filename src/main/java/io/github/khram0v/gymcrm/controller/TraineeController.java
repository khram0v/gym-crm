package io.github.khram0v.gymcrm.controller;

import io.github.khram0v.gymcrm.api.TraineeApi;
import io.github.khram0v.gymcrm.dto.request.ActivateRequest;
import io.github.khram0v.gymcrm.dto.request.ChangePasswordRequest;
import io.github.khram0v.gymcrm.dto.request.TraineeRegistrationRequest;
import io.github.khram0v.gymcrm.dto.request.UpdateTraineeRequest;
import io.github.khram0v.gymcrm.dto.request.UpdateTraineeTrainersRequest;
import io.github.khram0v.gymcrm.dto.response.RegistrationResponse;
import io.github.khram0v.gymcrm.dto.response.TraineeProfileResponse;
import io.github.khram0v.gymcrm.dto.response.TraineeTrainingResponse;
import io.github.khram0v.gymcrm.dto.response.TrainerSummary;
import io.github.khram0v.gymcrm.service.TraineeService;
import io.github.khram0v.gymcrm.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TraineeController implements TraineeApi {

    private final TraineeService traineeService;
    private final TrainingService trainingService;

    @Override
    public RegistrationResponse register(TraineeRegistrationRequest request) {
        return traineeService.create(
                request.firstName(), request.lastName(),
                request.dateOfBirth(), request.address());
    }

    @Override
    public TraineeProfileResponse getProfile(String username) {
        return traineeService.getByUsername(username);
    }

    @Override
    public void changePassword(String username, ChangePasswordRequest request) {
        traineeService.changePassword(username, request.oldPassword(), request.newPassword());
    }

    @Override
    public TraineeProfileResponse updateProfile(String username, UpdateTraineeRequest request) {
        return traineeService.updateProfile(
                username, request.firstName(), request.lastName(),
                request.dateOfBirth(),request.address(),request.active());
    }

    @Override
    public void delete(String username) {
        traineeService.deleteByUsername(username);
    }

    @Override
    public List<TrainerSummary> getUnassignedTrainers(String username) {
        return traineeService.getUnassignedTrainers(username);
    }

    @Override
    public List<TrainerSummary> updateTrainers(String username, UpdateTraineeTrainersRequest request) {
        return traineeService.updateTrainers(username, request.trainerUsernames());
    }

    @Override
    public List<TraineeTrainingResponse> getTrainings(String username,
                                                      LocalDate from,
                                                      LocalDate to,
                                                      String trainerFirstName,
                                                      String trainerLastName,
                                                      String trainingType) {
        return trainingService.getTraineeTrainings(
                username, from, to, trainerFirstName, trainerLastName, trainingType);
    }

    @Override
    public void setActiveStatus(String username, ActivateRequest request) {
        traineeService.setActiveStatus(username, request.active());
    }
}
