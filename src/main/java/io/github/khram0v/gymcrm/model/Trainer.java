package io.github.khram0v.gymcrm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trainers")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trainer extends User {

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "specialization_id", nullable = false)
    private TrainingType specialization;

    @OneToMany(mappedBy = "trainer")
    private List<Training> trainings = new ArrayList<>();

    @ManyToMany(mappedBy = "trainers")
    private List<Trainee> trainees = new ArrayList<>();

    public Trainer(String firstName, String lastName, TrainingType specialization) {
        super(firstName, lastName);
        this.specialization = specialization;
    }
}
