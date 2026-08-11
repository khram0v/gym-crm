package io.github.khram0v.gymcrm.security;

import io.github.khram0v.gymcrm.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordVerifier {

    private final PasswordEncoder passwordEncoder;

    public void verify(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new AuthenticationException("Invalid username or password");
        }
    }
}
