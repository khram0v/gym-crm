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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
public class TrainerController implements TrainerApi {

    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(@RequestBody TrainerRegistrationRequest request) {
        return trainerService.create(
                request.firstName(), request.lastName(), request.specializationId());
    }

    @Override
    @GetMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN') or @resourceGuard.isTrainerOwner(#username)")
    public TrainerProfileResponse getProfile(@PathVariable String username) {
        return trainerService.getByUsername(username);
    }

    @Override
    @PutMapping("/{username}/password")
    @PreAuthorize("hasRole('ADMIN') or @resourceGuard.isTrainerOwner(#username)")
    public void changePassword(@PathVariable String username,
                               @RequestBody ChangePasswordRequest request) {
        trainerService.changePassword(username, request.oldPassword(), request.newPassword());
    }

    @Override
    @PutMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN') or @resourceGuard.isTrainerOwner(#username)")
    public TrainerProfileResponse updateProfile(@PathVariable String username,
                                                @RequestBody UpdateTrainerRequest request) {
        return trainerService.updateProfile(
                username, request.firstName(), request.lastName(), request.active());
    }

    @Override
    @GetMapping("/{username}/trainings")
    @PreAuthorize("hasRole('ADMIN') or @resourceGuard.isTrainerOwner(#username)")
    public List<TrainerTrainingResponse> getTrainings(
            @PathVariable String username,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,
            @RequestParam(required = false) String traineeFirstName,
            @RequestParam(required = false) String traineeLastName) {
        return trainingService.getTrainerTrainings(username, from, to, traineeFirstName, traineeLastName);
    }

    @Override
    @PatchMapping("/{username}/status")
    @PreAuthorize("hasRole('ADMIN') or @resourceGuard.isTrainerOwner(#username)")
    public void setActiveStatus(@PathVariable String username,
                                @RequestBody ActivateRequest request) {
        trainerService.setActiveStatus(username, request.active());
    }
}
