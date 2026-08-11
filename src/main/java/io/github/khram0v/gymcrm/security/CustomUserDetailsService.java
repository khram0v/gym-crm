package io.github.khram0v.gymcrm.security;

import io.github.khram0v.gymcrm.model.User;
import io.github.khram0v.gymcrm.repository.TraineeRepository;
import io.github.khram0v.gymcrm.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final AdminProperties adminProperties;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (adminProperties.username().equals(username)) {
            return new UserPrincipal(adminProperties.username(), adminProperties.passwordHash(),
                    adminProperties.enabled(), Role.ADMIN);
        }

        return traineeRepository.findByUsername(username)
                .map(trainee -> toPrincipal(trainee, Role.TRAINEE))
                .or(() -> trainerRepository.findByUsername(username)
                        .map(trainer -> toPrincipal(trainer, Role.TRAINER)))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private UserPrincipal toPrincipal(User user, Role role) {
        return new UserPrincipal(user.getUsername(), user.getPassword(), user.isActive(), role);
    }
}
