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

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Trainers", description = "Trainer registration, profile and trainings")
public interface TrainerApi {

    @Operation(summary = "Register a new trainer")
    @ApiResponse(responseCode = "201", description = "Trainer created")
    @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Specialization not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @SecurityRequirements
    RegistrationResponse register(@Valid TrainerRegistrationRequest request);

    @Operation(summary = "Get trainer profile")
    @ApiResponse(responseCode = "200", description = "Profile found")
    @ApiResponse(responseCode = "401", description = "Unauthenticated",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Not the owner of this resource",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Trainer not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    TrainerProfileResponse getProfile(
            @Parameter(description = "Trainer username") String username);

    @Operation(summary = "Change trainer password")
    @ApiResponse(responseCode = "200", description = "Password changed")
    @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Invalid credentials or unauthenticated",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Not the owner of this resource",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    void changePassword(
            @Parameter(description = "Trainer username") String username,
            @Valid ChangePasswordRequest request);

    @Operation(summary = "Update trainer profile")
    @ApiResponse(responseCode = "200", description = "Profile updated")
    @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthenticated",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Not the owner of this resource",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Trainer not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    TrainerProfileResponse updateProfile(
            @Parameter(description = "Trainer username") String username,
            @Valid UpdateTrainerRequest request);

    @Operation(summary = "Get the trainer's trainings")
    @ApiResponse(responseCode = "200", description = "Trainings")
    @ApiResponse(responseCode = "401", description = "Unauthenticated",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Not the owner of this resource",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Trainer not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    List<TrainerTrainingResponse> getTrainings(
            @Parameter(description = "Trainer username") String username,
            @Parameter(description = "Period from (inclusive)") LocalDate from,
            @Parameter(description = "Period to (inclusive)") LocalDate to,
            @Parameter(description = "Trainee first name filter") String traineeFirstName,
            @Parameter(description = "Trainee last name filter") String traineeLastName);

    @Operation(summary = "Activate or deactivate a trainer")
    @ApiResponse(responseCode = "200", description = "Status changed")
    @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthenticated",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Not the owner of this resource",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Trainer not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Already in requested state",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    void setActiveStatus(
            @Parameter(description = "Trainer username") String username,
            @Valid ActivateRequest request);
}
