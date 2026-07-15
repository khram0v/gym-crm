package io.github.khram0v.gymcrm.repository;

import io.github.khram0v.gymcrm.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    Optional<Trainer> findByUsername(String username);

    boolean existsByUsername(String username);
}
