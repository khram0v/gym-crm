package io.github.khram0v.gymcrm.client.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "messaging")
public record MessagingProperties(
        @DefaultValue("trainer-workload.events") String trainerWorkloadEventsQueue
) {
}
