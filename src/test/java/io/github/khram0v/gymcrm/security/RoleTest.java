package io.github.khram0v.gymcrm.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    void authority_prependsRolePrefix() {
        assertThat(Role.TRAINEE.authority()).isEqualTo("ROLE_TRAINEE");
        assertThat(Role.TRAINER.authority()).isEqualTo("ROLE_TRAINER");
    }
}
