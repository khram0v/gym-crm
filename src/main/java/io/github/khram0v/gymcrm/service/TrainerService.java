package io.github.khram0v.gymcrm.service;

import io.github.khram0v.gymcrm.model.Trainer;

public interface TrainerService {

    Trainer create(String firstName, String lastName, Long specializationId);

    Trainer getByUsername(String username);

    void changePassword(String username, String oldPassword, String newPassword);

    Trainer updateProfile(String username, String firstName, String lastName, boolean active);

    void setActiveStatus(String username, boolean active);
}
