package io.github.khram0v.gymcrm.security;

import io.github.khram0v.gymcrm.exception.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordVerifierTest {

    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private PasswordVerifier passwordVerifier;

    @Test
    void verify_whenPasswordMatches_doesNotThrow() {
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);

        assertThatCode(() -> passwordVerifier.verify("raw", "encoded"))
                .doesNotThrowAnyException();
    }

    @Test
    void verify_whenPasswordDoesNotMatch_throwsAuthenticationException() {
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> passwordVerifier.verify("wrong", "encoded"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");
    }
}
