package io.github.khram0v.gymcrm.testsupport;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import tools.jackson.databind.ObjectMapper;

@TestConfiguration(proxyBeanMethods = false)
public class ApiClientTestConfiguration {

    @Bean
    @Lazy
    public ApiClient apiClient(@LocalServerPort int port, ObjectMapper objectMapper) {
        return new ApiClient(port, objectMapper);
    }
}
