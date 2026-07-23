package io.github.khram0v.gymcrm.dto.response;

import java.util.List;

public record TrainerProfileResponse(
        String username,
        String firstName,
        String lastName,
        TrainingTypeResponse specialization,
        boolean active,
        List<TraineeSummary> trainees
) {
}
