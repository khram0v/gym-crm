package io.github.khram0v.gymcrm.client.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "security.service")
public record ServiceJwtProperties(
    String secret,
    @DefaultValue("gym-crm-service") String subject,
    @DefaultValue("60000") long expirationMs
) {
}
