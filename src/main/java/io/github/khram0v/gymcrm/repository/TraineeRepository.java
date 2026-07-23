package io.github.khram0v.gymcrm.repository;

import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    Optional<Trainee> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("""
            SELECT tr FROM Trainer tr
            WHERE tr.isActive = true
              AND tr NOT IN (
                  SELECT t FROM Trainee te JOIN te.trainers t
                  WHERE te.username = :username
              )
            """)
    Set<Trainer> findUnassignedTrainers(@Param("username") String username);
}
