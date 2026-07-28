package io.github.khram0v.gymcrm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "trainings")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @NotNull
    @Column(name = "training_name", nullable = false)
    private String trainingName;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "training_type_id", nullable = false)
    private TrainingType trainingType;

    @NotNull
    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;

    @NotNull
    @Positive
    @Column(name = "training_duration", nullable = false)
    private Integer trainingDuration;

    public Training(Trainee trainee, Trainer trainer, String trainingName,
                    TrainingType trainingType, LocalDate trainingDate, Integer trainingDuration) {
        this.trainee = trainee;
        this.trainer = trainer;
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Training training = (Training) o;
        return id != null && Objects.equals(id, training.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
