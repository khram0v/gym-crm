package io.github.khram0v.gymcrm.service;

import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface TraineeService {

    Trainee create(String firstName, String lastName, LocalDate dateOfBirth, String address);

    Trainee getByUsername(String username);

    void changePassword(String username, String oldPassword, String newPassword);

    Trainee updateProfile(String username,
                          String firstName,
                          String lastName,
                          LocalDate dateOfBirth,
                          String address);

    void setActiveStatus(String username, boolean active);

    void deleteByUsername(String username);

    Set<Trainer> getUnassignedTrainers(String username);

    Set<Trainer> updateTrainers(String username, List<String> trainerUsernames);
}
