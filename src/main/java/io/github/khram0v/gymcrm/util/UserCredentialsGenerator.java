package io.github.khram0v.gymcrm.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.function.Predicate;

@Component
@Slf4j
public class UserCredentialsGenerator {

    private static final String USERNAME_SEPARATOR = ".";
    private static final int PASSWORD_LENGTH = 10;
    private static final String PASSWORD_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final SecureRandom random = new SecureRandom();

    public String generateUsername(String firstName, String lastName, Predicate<String> usernameExists) {
        String base = firstName + USERNAME_SEPARATOR + lastName;

        if (!usernameExists.test(base)) {
            log.debug("Generated username '{}'", base);
            return base;
        }

        int suffix = 1;
        String candidate = base + suffix;
        while (usernameExists.test(candidate)) {
            suffix++;
            candidate = base + suffix;
        }

        log.debug("Username '{}' already taken, generated '{}'", base, candidate);
        return candidate;
    }

    public String generatePassword() {
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = random.nextInt(PASSWORD_CHARS.length());
            sb.append(PASSWORD_CHARS.charAt(index));
        }

        log.debug("Generated a random password of length {}", PASSWORD_LENGTH);
        return sb.toString();
    }
}
