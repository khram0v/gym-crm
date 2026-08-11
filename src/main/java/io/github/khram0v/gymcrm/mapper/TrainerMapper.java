package io.github.khram0v.gymcrm.mapper;

import io.github.khram0v.gymcrm.dto.response.TrainerProfileResponse;
import io.github.khram0v.gymcrm.model.Trainer;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = {SummaryMapper.class, TrainingTypeMapper.class})
public interface TrainerMapper {

    @Mapping(source = "specialization", target = "specialization")
    @Mapping(source = "trainees", target = "trainees", qualifiedByName = "traineeSetToSortedSummaries")
    TrainerProfileResponse toProfileResponse(Trainer trainer);
}
