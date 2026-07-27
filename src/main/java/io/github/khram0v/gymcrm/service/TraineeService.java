package io.github.khram0v.gymcrm.service;

import io.github.khram0v.gymcrm.dto.response.RegistrationResponse;
import io.github.khram0v.gymcrm.dto.response.TraineeProfileResponse;
import io.github.khram0v.gymcrm.dto.response.TrainerSummary;

import java.time.LocalDate;
import java.util.List;

public interface TraineeService {

    RegistrationResponse create(String firstName, String lastName, LocalDate dateOfBirth, String address);

    TraineeProfileResponse getByUsername(String username);

    void changePassword(String username, String oldPassword, String newPassword);

    TraineeProfileResponse updateProfile(String username,
                          String firstName,
                          String lastName,
                          LocalDate dateOfBirth,
                          String address,
                          boolean active);

    void deleteByUsername(String username);

    List<TrainerSummary> getUnassignedTrainers(String username);

    List<TrainerSummary> updateTrainers(String username, List<String> trainerUsernames);

    void setActiveStatus(String username, boolean active);
}
