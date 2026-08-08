package io.github.khram0v.gymcrm.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ResourceGuard {

    public boolean isTraineeOwner(String username) {
        return hasRoleAndUsername(Role.TRAINEE, username);
    }

    public boolean isTrainerOwner(String username) {
        return hasRoleAndUsername(Role.TRAINER, username);
    }

    private boolean hasRoleAndUsername (Role role, String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }
        return principal.getRole() == role && principal.getUsername().equals(username);
    }
}
