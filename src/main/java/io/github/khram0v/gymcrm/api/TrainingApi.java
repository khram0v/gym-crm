package io.github.khram0v.gymcrm.api;

import io.github.khram0v.gymcrm.dto.request.AddTrainingRequest;
import io.github.khram0v.gymcrm.dto.response.ErrorResponse;
import io.github.khram0v.gymcrm.dto.response.TrainingTypeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

@Tag(name = "Trainings", description = "Training session management")
public interface TrainingApi {

    @Operation(summary = "Add a training session")
    @ApiResponse(responseCode = "201", description = "Training created")
    @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthenticated",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Not the owner of this resource",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Trainer or trainee not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    void addTraining(@Valid AddTrainingRequest request);

    @Operation(summary = "Get all training types")
    @ApiResponse(responseCode = "200", description = "Training types")
    @ApiResponse(responseCode = "401", description = "Unauthenticated",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    List<TrainingTypeResponse> getAllTrainingTypes();
}
