package io.github.khram0v.gymcrm.service;

import io.github.khram0v.gymcrm.dto.response.RegistrationResponse;
import io.github.khram0v.gymcrm.dto.response.TrainerProfileResponse;

public interface TrainerService {

    RegistrationResponse create(String firstName, String lastName, Long specializationId);

    TrainerProfileResponse getByUsername(String username);

    void changePassword(String username, String oldPassword, String newPassword);

    TrainerProfileResponse updateProfile(String username, String firstName, String lastName, boolean active);

    void setActiveStatus(String username, boolean active);
}
