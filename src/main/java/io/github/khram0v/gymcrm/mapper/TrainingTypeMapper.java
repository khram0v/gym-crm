package io.github.khram0v.gymcrm.mapper;

import io.github.khram0v.gymcrm.dto.response.TrainingTypeResponse;
import io.github.khram0v.gymcrm.model.TrainingType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TrainingTypeMapper {

    @Mapping(source = "trainingTypeName", target = "name")
    TrainingTypeResponse toResponse(TrainingType trainingType);

    List<TrainingTypeResponse> toResponseList(List<TrainingType> trainingTypes);
}
