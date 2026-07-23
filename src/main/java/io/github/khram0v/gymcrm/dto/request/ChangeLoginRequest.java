package io.github.khram0v.gymcrm.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeLoginRequest(
        @NotBlank String username,
        @NotBlank String oldPassword,
        @NotBlank String newPassword
) {
}
