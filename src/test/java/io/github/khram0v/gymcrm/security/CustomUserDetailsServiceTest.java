package io.github.khram0v.gymcrm.security;

import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.repository.TraineeRepository;
import io.github.khram0v.gymcrm.repository.TrainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private TraineeRepository traineeRepository;
    @Mock private TrainerRepository trainerRepository;

    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        AdminProperties adminProperties = new AdminProperties("admin", "encodedAdminPass", true);
        userDetailsService = new CustomUserDetailsService(traineeRepository, trainerRepository, adminProperties);
    }

    @Test
    void loadUserByUsername_whenTraineeExists_returnsPrincipalWithTraineeRole() {
        Trainee trainee = new Trainee("John", "Doe", null, null);
        trainee.setUsername("John.Doe");
        trainee.setPassword("encodedPass");
        trainee.setActive(true);
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        UserDetails result = userDetailsService.loadUserByUsername("John.Doe");

        assertThat(result).isInstanceOf(UserPrincipal.class);
        assertThat(result.getUsername()).isEqualTo("John.Doe");
        assertThat(result.getPassword()).isEqualTo("encodedPass");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_TRAINEE");
        verifyNoInteractions(trainerRepository);
    }

    @Test
    void loadUserByUsername_whenOnlyTrainerExists_returnsPrincipalWithTrainerRole() {
        Trainer trainer = new Trainer("Jane", "Smith", null);
        trainer.setUsername("Jane.Smith");
        trainer.setPassword("encodedPass");
        trainer.setActive(false);
        when(traineeRepository.findByUsername("Jane.Smith")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainer));

        UserDetails result = userDetailsService.loadUserByUsername("Jane.Smith");

        assertThat(result.getUsername()).isEqualTo("Jane.Smith");
        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_TRAINER");
    }

    @Test
    void loadUserByUsername_whenNeitherExists_throwsUsernameNotFoundException() {
        when(traineeRepository.findByUsername("Ghost")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("Ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("Ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found: Ghost");
    }

    @Test
    void loadUserByUsername_whenUsernameMatchesAdmin_returnsPrincipalWithAdminRole_andDoesNotQueryRepositories() {
        UserDetails result = userDetailsService.loadUserByUsername("admin");

        assertThat(result).isInstanceOf(UserPrincipal.class);
        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getPassword()).isEqualTo("encodedAdminPass");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
        verifyNoInteractions(traineeRepository, trainerRepository);
    }

    @Test
    void loadUserByUsername_whenAdminDisabledInConfig_returnsDisabledPrincipal() {
        AdminProperties disabledAdmin = new AdminProperties("admin", "encodedAdminPass", false);
        CustomUserDetailsService serviceWithDisabledAdmin =
                new CustomUserDetailsService(traineeRepository, trainerRepository, disabledAdmin);

        UserDetails result = serviceWithDisabledAdmin.loadUserByUsername("admin");

        assertThat(result.isEnabled()).isFalse();
    }
}
