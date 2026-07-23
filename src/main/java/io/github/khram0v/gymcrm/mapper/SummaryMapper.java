package io.github.khram0v.gymcrm.mapper;

import io.github.khram0v.gymcrm.dto.response.TraineeSummary;
import io.github.khram0v.gymcrm.dto.response.TrainerSummary;
import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = {TrainingTypeMapper.class})
public interface SummaryMapper {

    @Mapping(source = "specialization", target = "specialization")
    TrainerSummary toTrainerSummary(Trainer trainer);

    TraineeSummary toTraineeSummary(Trainee trainee);

    @Named("trainerSetToSortedSummaries")
    default List<TrainerSummary> trainerSetToSortedSummaries(Set<Trainer> trainers) {
        if (trainers == null) {
            return List.of();
        }
        return trainers.stream()
                .sorted(Comparator.comparing(Trainer::getUsername))
                .map(this::toTrainerSummary)
                .collect(Collectors.toList());
    }

    @Named("traineeSetToSortedSummaries")
    default List<TraineeSummary> traineeSetToSortedSummaries(Set<Trainee> trainees) {
        if (trainees == null) {
            return List.of();
        }
        return trainees.stream()
                .sorted(Comparator.comparing(Trainee::getUsername))
                .map(this::toTraineeSummary)
                .collect(Collectors.toList());
    }
}
