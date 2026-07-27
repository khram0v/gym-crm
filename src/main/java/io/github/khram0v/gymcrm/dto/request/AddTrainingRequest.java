package io.github.khram0v.gymcrm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record AddTrainingRequest(
        @NotBlank String trainerUsername,
        @NotBlank String traineeUsername,
        @NotBlank String trainingName,
        @NotNull LocalDate trainingDate,
        @NotNull @Positive Integer trainingDuration
) {
}
