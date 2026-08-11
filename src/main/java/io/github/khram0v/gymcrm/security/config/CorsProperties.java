package io.github.khram0v.gymcrm.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties(prefix = "security.cors")
public record CorsProperties(
        @DefaultValue({"*"}) List<String> allowedOrigins
) {
}
