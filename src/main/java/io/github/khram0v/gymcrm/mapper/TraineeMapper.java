package io.github.khram0v.gymcrm.mapper;

import io.github.khram0v.gymcrm.dto.response.TraineeProfileResponse;
import io.github.khram0v.gymcrm.model.Trainee;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = {SummaryMapper.class})
public interface TraineeMapper {

    @Mapping(source = "trainers", target = "trainers", qualifiedByName = "trainerSetToSortedSummaries")
    TraineeProfileResponse toProfileResponse(Trainee trainee);
}
