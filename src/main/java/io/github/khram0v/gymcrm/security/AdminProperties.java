package io.github.khram0v.gymcrm.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "security.admin")
public record AdminProperties(
        String username,
        String passwordHash,
        @DefaultValue("true") boolean enabled
) {
}
