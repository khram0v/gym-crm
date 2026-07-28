package io.github.khram0v.gymcrm.dto.response;

import java.time.LocalDate;

public record TrainerTrainingResponse(
        String trainingName,
        LocalDate trainingDate,
        String trainingType,
        Integer trainingDuration,
        String traineeFirstName,
        String traineeLastName
) {
}
