package io.github.khram0v.gymcrm.repository;

import io.github.khram0v.gymcrm.model.TrainingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainingTypeRepository extends JpaRepository<TrainingType, Long> {

    Optional<TrainingType> findByTrainingTypeName(String trainingTypeName);
}
