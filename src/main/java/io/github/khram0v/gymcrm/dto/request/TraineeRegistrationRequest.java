package io.github.khram0v.gymcrm.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record TraineeRegistrationRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        LocalDate dateOfBirth,
        String address
) {
}
