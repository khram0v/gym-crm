package io.github.khram0v.gymcrm.testsupport.security;

import io.github.khram0v.gymcrm.repository.TrainingRepository;
import io.github.khram0v.gymcrm.security.ResourceGuard;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@TestConfiguration
@EnableMethodSecurity(proxyTargetClass = true)
public class MethodSecurityTestConfig {

    @Bean
    public ResourceGuard resourceGuard(TrainingRepository trainingRepository) {
        return new ResourceGuard(trainingRepository);
    }
}
