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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trainees")
@RequiredArgsConstructor
public class TraineeController implements TraineeApi {

    private final TraineeService traineeService;
    private final TrainingService trainingService;

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(@RequestBody TraineeRegistrationRequest request) {
        return traineeService.create(
                request.firstName(), request.lastName(),
                request.dateOfBirth(), request.address());
    }

    @Override
    @GetMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN') or @resourceGuard.isTraineeOwner(#username)")
    public TraineeProfileResponse getProfile(@PathVariable String username) {
        return traineeService.getByUsername(username);
    }

    @Override
    @PutMapping("/{username}/password")
    @PreAuthorize("hasRole('ADMIN') or @resourceGuard.isTraineeOwner(#username)")
    public void changePassword(@PathVariable String username,
                               @RequestBody ChangePasswordRequest request) {
        traineeService.changePassword(username, request.oldPassword(), request.newPassword());
    }

    @Override
    @PutMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN') or @resourceGuard.isTraineeOwner(#username)")
    public TraineeProfileResponse updateProfile(@PathVariable String username,
                                                @RequestBody UpdateTraineeRequest request) {
        return traineeService.updateProfile(
                username, request.firstName(), request.lastName(),
                request.dateOfBirth(),request.address(),request.active());
    }

    @Override
    @DeleteMapping("/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN') or @resourceGuard.isTraineeOwner(#username)")
    public void delete(@PathVariable String username) {
        traineeService.deleteByUsername(username);
    }

    @Override
    @GetMapping("/{username}/unassigned-trainers")
    @PreAuthorize("hasRole('ADMIN') or @resourceGuard.isTraineeOwner(#username)")
    public List<TrainerSummary> getUnassignedTrainers(@PathVariable String username) {
        return traineeService.getUnassignedTrainers(username);
    }

    @Override
    @PutMapping("/{username}/trainers")
    @PreAuthorize("hasRole('ADMIN') or @resourceGuard.isTraineeOwner(#username)")
    public List<TrainerSummary> updateTrainers(@PathVariable String username,
                                               @RequestBody UpdateTraineeTrainersRequest request) {
        return traineeService.updateTrainers(username, request.trainerUsernames());
    }

    @Override
    @GetMapping("/{username}/trainings")
    @PreAuthorize("hasRole('ADMIN') or @resourceGuard.isTraineeOwner(#username)")
    public List<TraineeTrainingResponse> getTrainings(
            @PathVariable String username,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,
            @RequestParam(required = false) String trainerFirstName,
            @RequestParam(required = false) String trainerLastName,
            @RequestParam(required = false) String trainingType) {
        return trainingService.getTraineeTrainings(
                username, from, to, trainerFirstName, trainerLastName, trainingType);
    }

    @Override
    @PatchMapping("/{username}/status")
    @PreAuthorize("hasRole('ADMIN') or @resourceGuard.isTraineeOwner(#username)")
    public void setActiveStatus(@PathVariable String username,
                                @RequestBody ActivateRequest request) {
        traineeService.setActiveStatus(username, request.active());
    }
}
