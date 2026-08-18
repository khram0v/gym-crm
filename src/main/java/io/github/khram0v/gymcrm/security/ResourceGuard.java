package io.github.khram0v.gymcrm.security;

import io.github.khram0v.gymcrm.repository.TrainingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceGuard {

    private final TrainingRepository trainingRepository;

    public boolean isTraineeOwner(String username) {
        return hasRoleAndUsername(Role.TRAINEE, username);
    }

    public boolean isTrainerOwner(String username) {
        return hasRoleAndUsername(Role.TRAINER, username);
    }

    public boolean isTrainingOwner(Long trainingId) {
        UserPrincipal principal = currentPrincipal();
        if (principal == null || principal.getRole() != Role.TRAINER) {
            return false;
        }
        return trainingRepository.findById(trainingId)
                .map(training -> training.getTrainer().getUsername().equals(principal.getUsername()))
                .orElse(true); // request falls through to the service layer, which reports 404 instead of 403
    }

    private boolean hasRoleAndUsername(Role role, String username) {
        UserPrincipal principal = currentPrincipal();
        return principal != null && principal.getRole().equals(role) && principal.getUsername().equals(username);
    }

    private UserPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return null;
        }
        return principal;
    }
}
