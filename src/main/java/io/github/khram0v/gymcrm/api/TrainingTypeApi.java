package io.github.khram0v.gymcrm.api;

import io.github.khram0v.gymcrm.dto.response.ErrorResponse;
import io.github.khram0v.gymcrm.dto.response.TrainingTypeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Tag(name = "Training Types", description = "Reference data")
@RequestMapping("/api/v1/training-types")
public interface TrainingTypeApi {

    @Operation(summary = "Get all training types")
    @ApiResponse(responseCode = "200", description = "Training types")
    @ApiResponse(responseCode = "401", description = "Unauthenticated",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    List<TrainingTypeResponse> getAll();
}
