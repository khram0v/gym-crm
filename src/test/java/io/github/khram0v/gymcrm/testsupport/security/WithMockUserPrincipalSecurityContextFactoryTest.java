package io.github.khram0v.gymcrm.testsupport.security;

import io.github.khram0v.gymcrm.security.Role;
import io.github.khram0v.gymcrm.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WithMockUserPrincipalSecurityContextFactoryTest {

    private final WithMockUserPrincipalSecurityContextFactory factory =
            new WithMockUserPrincipalSecurityContextFactory();

    @Test
    void createSecurityContext_buildsAuthenticatedUserPrincipal() {
        WithMockUserPrincipal annotation = mock(WithMockUserPrincipal.class);
        when(annotation.username()).thenReturn("John.Doe");
        when(annotation.role()).thenReturn(Role.TRAINEE);

        SecurityContext context = factory.createSecurityContext(annotation);

        assertThat(context.getAuthentication()).isNotNull();
        assertThat(context.getAuthentication().isAuthenticated()).isTrue();

        UserPrincipal principal = (UserPrincipal) context.getAuthentication().getPrincipal();
        assertThat(principal.getUsername()).isEqualTo("John.Doe");
        assertThat(principal.getRole()).isEqualTo(Role.TRAINEE);
    }
}
