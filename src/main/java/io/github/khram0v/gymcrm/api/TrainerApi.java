package io.github.khram0v.gymcrm.api;

import io.github.khram0v.gymcrm.dto.request.ActivateRequest;
import io.github.khram0v.gymcrm.dto.request.ChangePasswordRequest;
import io.github.khram0v.gymcrm.dto.request.TrainerRegistrationRequest;
import io.github.khram0v.gymcrm.dto.request.UpdateTrainerRequest;
import io.github.khram0v.gymcrm.dto.response.ErrorResponse;
import io.github.khram0v.gymcrm.dto.response.RegistrationResponse;
import io.github.khram0v.gymcrm.dto.response.TrainerProfileResponse;
import io.github.khram0v.gymcrm.dto.response.TrainerTrainingResponse;
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

@Tag(name = "Trainers", description = "Trainer registration, profile and trainings")
@RequestMapping("/api/v1/trainers")
public interface TrainerApi {

    @Operation(summary = "Register a new trainer")
    @ApiResponse(responseCode = "201", description = "Trainer created")
    @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Specialization not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @SecurityRequirements
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    RegistrationResponse register(@Valid @RequestBody TrainerRegistrationRequest request);

    @Operation(summary = "Get trainer profile")
    @ApiResponse(responseCode = "200", description = "Profile found")
    @ApiResponse(responseCode = "401", description = "Unauthenticated",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Trainer not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{username}")
    @ResponseStatus(HttpStatus.OK)
    TrainerProfileResponse getProfile(
            @Parameter(description = "Trainer username") @PathVariable String username);

    @Operation(summary = "Change trainer password")
    @ApiResponse(responseCode = "200", description = "Password changed")
    @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @SecurityRequirements
    @PutMapping("/{username}/password")
    @ResponseStatus(HttpStatus.OK)
    void changePassword(
            @Parameter(description = "Trainer username") @PathVariable String username,
            @Valid @RequestBody ChangePasswordRequest request);

    @Operation(summary = "Update trainer profile")
    @ApiResponse(responseCode = "200", description = "Profile updated")
    @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthenticated",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Trainer not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/{username}")
    @ResponseStatus(HttpStatus.OK)
    TrainerProfileResponse updateProfile(
            @Parameter(description = "Trainer username") @PathVariable String username,
            @Valid @RequestBody UpdateTrainerRequest request);

    @Operation(summary = "Get the trainer's trainings")
    @ApiResponse(responseCode = "200", description = "Trainings")
    @ApiResponse(responseCode = "401", description = "Unauthenticated",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Trainer not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{username}/trainings")
    @ResponseStatus(HttpStatus.OK)
    List<TrainerTrainingResponse> getTrainings(
            @Parameter(description = "Trainer username") @PathVariable String username,
            @Parameter(description = "Period from (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Period to (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Trainee first name filter")
            @RequestParam(required = false) String traineeFirstName,
            @Parameter(description = "Trainee last name filter")
            @RequestParam(required = false) String traineeLastName);

    @Operation(summary = "Activate or deactivate a trainer")
    @ApiResponse(responseCode = "200", description = "Status changed")
    @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthenticated",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Trainer not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Already in requested state",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping("/{username}/status")
    @ResponseStatus(HttpStatus.OK)
    void setActiveStatus(
            @Parameter(description = "Trainer username") @PathVariable String username,
            @Valid @RequestBody ActivateRequest request);
}
