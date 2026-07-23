package io.github.khram0v.gymcrm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateTraineeTrainerRequest(
        @NotEmpty List<@NotBlank String> trainerUsernames
) {
}
