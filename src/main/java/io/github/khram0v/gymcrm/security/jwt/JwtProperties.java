package io.github.khram0v.gymcrm.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String secret,
        @DefaultValue("3600000") long expirationMs
) {
}
