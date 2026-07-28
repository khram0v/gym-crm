package io.github.khram0v.gymcrm.dto.response;

import java.time.LocalDate;

public record TraineeTrainingResponse(
        String trainingName,
        LocalDate trainingDate,
        String trainingType,
        Integer trainingDuration,
        String trainerFirstName,
        String trainerLastName
) {
}
