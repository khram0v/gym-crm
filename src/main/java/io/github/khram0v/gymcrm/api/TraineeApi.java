package io.github.khram0v.gymcrm.api;

import io.github.khram0v.gymcrm.dto.request.ActivateRequest;
import io.github.khram0v.gymcrm.dto.request.ChangePasswordRequest;
import io.github.khram0v.gymcrm.dto.request.TraineeRegistrationRequest;
import io.github.khram0v.gymcrm.dto.request.UpdateTraineeRequest;
import io.github.khram0v.gymcrm.dto.request.UpdateTraineeTrainersRequest;
import io.github.khram0v.gymcrm.dto.response.ErrorResponse;
import io.github.khram0v.gymcrm.dto.response.RegistrationResponse;
import io.github.khram0v.gymcrm.dto.response.TraineeProfileResponse;
import io.github.khram0v.gymcrm.dto.response.TraineeTrainingResponse;
import io.github.khram0v.gymcrm.dto.response.TrainerSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Trainees", description = "Trainee registration, profile and relationships")
@RequestMapping("/api/v1/trainees")
public interface TraineeApi {

    @Operation(summary = "Register a new trainee")
    @ApiResponse(responseCode = "201", description = "Trainee created")
    @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @SecurityRequirements
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    RegistrationResponse register(@Valid @RequestBody TraineeRegistrationRequest request);

    @Operation(summary = "Get trainee profile")
    @ApiResponse(responseCode = "200", description = "Profile found")
    @ApiResponse(responseCode = "401", description = "Unauthenticated",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Trainee not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{username}")
    @ResponseStatus(HttpStatus.OK)
    TraineeProfileResponse getProfile(
            @Parameter(description = "Trainee username") @PathVariable String username);

    @Operation(summary = "Change trainee password")
    @ApiResponse(responseCode = "200", description = "Password changed")
    @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @SecurityRequirements
    @PutMapping("/{username}/password")
    @ResponseStatus(HttpStatus.OK)
    void changePassword(
            @Parameter(description = "Trainee username") @PathVariable String username,
            @Valid @RequestBody ChangePasswordRequest request);

    @Operation(summary = "Update trainee profile")
    @ApiResponse(responseCode = "200", description = "Profile updated")
    @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthenticated",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Trainee not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/{username}")
    @ResponseStatus(HttpStatus.OK)
    TraineeProfileResponse updateProfile(
            @Parameter(description = "Trainee username") @PathVariable String username,
            @Valid @RequestBody UpdateTraineeRequest request);

    @Operation(summary = "Delete trainee profile")
    @ApiResponse(responseCode = "204", description = "Trainee deleted")
    @ApiResponse(responseCode = "401", description = "Unauthenticated",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Trainee not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@Parameter(description = "Trainee username") @PathVariable String username);

    @Operation(summary = "Get active trainers not assigned to this trainee")
    @ApiResponse(responseCode = "200", description = "Unassigned trainers")
    @ApiResponse(responseCode = "401", description = "Unauthenticated",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Trainee not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{username}/unassigned-trainers")
    @ResponseStatus(HttpStatus.OK)
    List<TrainerSummary> getUnassignedTrainers(
            @Parameter(description = "Trainee username") @PathVariable String username);

    @Operation(summary = "Update the trainee's assigned trainers list")
    @ApiResponse(responseCode = "200", description = "Trainers updated")
    @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthenticated",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Trainee or trainer not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/{username}/trainers")
    @ResponseStatus(HttpStatus.OK)
    List<TrainerSummary> updateTrainers(
            @Parameter(description = "Trainee username") @PathVariable String username,
            @Valid @RequestBody UpdateTraineeTrainersRequest request);

    @Operation(summary = "Get the trainee's trainings")
    @ApiResponse(responseCode = "200", description = "Trainings")
    @ApiResponse(responseCode = "401", description = "Unauthenticated",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Trainee not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{username}/trainings")
    @ResponseStatus(HttpStatus.OK)
    List<TraineeTrainingResponse> getTrainings(
            @Parameter(description = "Trainee username") @PathVariable String username,
            @Parameter(description = "Period from (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Period to (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Trainer first name filter")
            @RequestParam(required = false) String trainerFirstName,
            @Parameter(description = "Trainer last name filter")
            @RequestParam(required = false) String trainerLastName,
            @Parameter(description = "Training type name filter")
            @RequestParam(required = false) String trainingType);

    @Operation(summary = "Activate or deactivate a trainee")
    @ApiResponse(responseCode = "200", description = "Status changed")
    @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthenticated",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Trainee not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Already in requested state",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping("/{username}/status")
    @ResponseStatus(HttpStatus.OK)
    void setActiveStatus(
            @Parameter(description = "Trainee username") @PathVariable String username,
            @Valid @RequestBody ActivateRequest request);
}
