package io.github.khram0v.gymcrm.testsupport;

import io.github.khram0v.gymcrm.client.TrainerWorkloadClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration(proxyBeanMethods = false)
public class WorkloadClientTestConfiguration {

    @Bean
    @ServiceConnection
    public TrainerWorkloadClient trainerWorkloadClient() {
        return mock(TrainerWorkloadClient.class);
    }
}
