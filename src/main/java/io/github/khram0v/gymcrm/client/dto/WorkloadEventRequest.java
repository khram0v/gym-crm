package io.github.khram0v.gymcrm.client.dto;

import java.time.LocalDate;

public record WorkloadEventRequest(
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        Boolean active,
        LocalDate trainingDate,
        Integer trainingDuration,
        ActionType actionType
) {
}
