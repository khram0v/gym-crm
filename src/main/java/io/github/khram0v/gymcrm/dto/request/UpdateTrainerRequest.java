package io.github.khram0v.gymcrm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTrainerRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        Long specializationId,
        @NotNull Boolean active
) {
}
