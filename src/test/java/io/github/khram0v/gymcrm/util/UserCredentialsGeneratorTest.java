package io.github.khram0v.gymcrm.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class UserCredentialsGeneratorTest {

    private UserCredentialsGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new UserCredentialsGenerator();
    }

    @Test
    void shouldGenerateBaseUsernameWhenNoCollision() {
        Predicate<String> noneExist = username -> false;

        String result = generator.generateUsername("John", "Doe", noneExist);

        assertThat(result).isEqualTo("John.Doe");
    }

    @Test
    void shouldAppendOneWhenBaseTaken() {
        Set<String> existing = Set.of("John.Doe");

        String result = generator.generateUsername("John", "Doe", existing::contains);

        assertThat(result).isEqualTo("John.Doe1");
    }

    @Test
    void shouldAppendNextAvailableWhenMultipleTaken() {
        Set<String> existing = Set.of("John.Doe", "John.Doe1", "John.Doe2");

        String result = generator.generateUsername("John", "Doe", existing::contains);

        assertThat(result).isEqualTo("John.Doe3");
    }

    @Test
    void shouldReuseFreedSuffix() {
        Set<String> existing = Set.of("John.Doe", "John.Doe2");

        String result = generator.generateUsername("John", "Doe", existing::contains);

        assertThat(result).isEqualTo("John.Doe1");
    }

    @Test
    void shouldNotCollideForDifferentNames() {
        Set<String> existing = Set.of("John.Doe");

        String result = generator.generateUsername("Jane", "Doe", existing::contains);

        assertThat(result).isEqualTo("Jane.Doe");
    }

    @Test
    void shouldGeneratePasswordOfLengthTen() {
        String password = generator.generatePassword();

        assertThat(password).isNotNull().hasSize(10);
    }

    @RepeatedTest(10)
    void shouldGenerateDifferentPasswordsEachTime() {
        String p1 = generator.generatePassword();
        String p2 = generator.generatePassword();

        assertThat(p1).isNotEqualTo(p2);
    }

    @Test
    void shouldGeneratePasswordWithOnlyAllowedChars() {
        String password = generator.generatePassword();

        assertThat(password).matches("[A-Za-z0-9]+");
    }
}
