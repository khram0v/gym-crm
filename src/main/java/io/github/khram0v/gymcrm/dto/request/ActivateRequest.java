package io.github.khram0v.gymcrm.dto.request;

import jakarta.validation.constraints.NotNull;

public record ActivateRequest(
        @NotNull Boolean active
) {
}
