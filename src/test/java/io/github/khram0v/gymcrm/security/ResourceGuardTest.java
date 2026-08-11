package io.github.khram0v.gymcrm.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceGuardTest {

    private final ResourceGuard resourceGuard = new ResourceGuard();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void isTraineeOwner_whenPrincipalIsMatchingTrainee_returnsTrue() {
        authenticateAs("John.Doe", Role.TRAINEE);

        assertThat(resourceGuard.isTraineeOwner("John.Doe")).isTrue();
    }

    @Test
    void isTraineeOwner_whenUsernameDiffers_returnsFalse() {
        authenticateAs("John.Doe", Role.TRAINEE);

        assertThat(resourceGuard.isTraineeOwner("Someone.Else")).isFalse();
    }

    @Test
    void isTraineeOwner_whenPrincipalIsTrainer_returnsFalse() {
        authenticateAs("John.Doe", Role.TRAINER);

        assertThat(resourceGuard.isTraineeOwner("John.Doe")).isFalse();
    }

    @Test
    void isTraineeOwner_whenNoAuthentication_returnsFalse() {
        assertThat(resourceGuard.isTraineeOwner("John.Doe")).isFalse();
    }

    @Test
    void isTraineeOwner_whenPrincipalIsNotUserPrincipal_returnsFalse() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymousUser", null));

        assertThat(resourceGuard.isTraineeOwner("anonymousUser")).isFalse();
    }

    @Test
    void isTrainerOwner_whenPrincipalIsMatchingTrainer_returnsTrue() {
        authenticateAs("Jane.Smith", Role.TRAINER);

        assertThat(resourceGuard.isTrainerOwner("Jane.Smith")).isTrue();
    }

    @Test
    void isTrainerOwner_whenUsernameDiffers_returnsFalse() {
        authenticateAs("Jane.Smith", Role.TRAINER);

        assertThat(resourceGuard.isTrainerOwner("Someone.Else")).isFalse();
    }

    @Test
    void isTrainerOwner_whenPrincipalIsTrainee_returnsFalse() {
        authenticateAs("Jane.Smith", Role.TRAINEE);

        assertThat(resourceGuard.isTrainerOwner("Jane.Smith")).isFalse();
    }

    private void authenticateAs(String username, Role role) {
        UserPrincipal principal = new UserPrincipal(username, "encodedPass", true, role);
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
