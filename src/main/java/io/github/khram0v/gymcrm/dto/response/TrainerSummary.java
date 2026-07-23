package io.github.khram0v.gymcrm.dto.response;

public record TrainerSummary(
        String username,
        String firstName,
        String lastName,
        TrainingTypeResponse specialization
) {
}
