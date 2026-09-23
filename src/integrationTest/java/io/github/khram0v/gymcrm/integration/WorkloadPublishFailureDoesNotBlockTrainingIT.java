package io.github.khram0v.gymcrm.integration;

import io.github.khram0v.gymcrm.integration.support.HttpTestClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WorkloadPublishFailureDoesNotBlockTrainingIT {

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.activemq.broker-url", () -> "tcp://localhost:1");
    }

    @LocalServerPort
    private int port;

    @Test
    void addingATraining_stillSucceeds_whenTheBrokerIsUnreachable() {
        HttpTestClient client = new HttpTestClient("http://localhost:" + port);

        Map<String, Object> trainerBody = new LinkedHashMap<>();
        trainerBody.put("firstName", "NoBroker");
        trainerBody.put("lastName", "Trainer" + System.nanoTime());
        trainerBody.put("specializationId", 1);
        HttpResponse<String> trainerResponse = client.post("/api/v1/trainers", trainerBody, null);
        assertThat(trainerResponse.statusCode()).isEqualTo(201);
        JsonNode trainerJson = client.json(trainerResponse);
        String trainerUsername = trainerJson.get("username").asString();
        String trainerPassword = trainerJson.get("password").asString();

        Map<String, Object> traineeBody = new LinkedHashMap<>();
        traineeBody.put("firstName", "NoBroker");
        traineeBody.put("lastName", "Trainee" + System.nanoTime());
        HttpResponse<String> traineeResponse = client.post("/api/v1/trainees", traineeBody, null);
        assertThat(traineeResponse.statusCode()).isEqualTo(201);
        String traineeUsername = client.json(traineeResponse).get("username").asString();

        Map<String, Object> loginBody = Map.of("username", trainerUsername, "password", trainerPassword);
        HttpResponse<String> loginResponse = client.post("/api/v1/auth/login", loginBody, null);
        assertThat(loginResponse.statusCode()).isEqualTo(200);
        String token = client.json(loginResponse).get("token").asString();

        Map<String, Object> trainingBody = new LinkedHashMap<>();
        trainingBody.put("trainerUsername", trainerUsername);
        trainingBody.put("traineeUsername", traineeUsername);
        trainingBody.put("trainingName", "Session despite broker outage");
        trainingBody.put("trainingDate", "2024-06-10");
        trainingBody.put("trainingDuration", 45);

        HttpResponse<String> addResponse = client.post("/api/v1/trainings", trainingBody, token);

        assertThat(addResponse.statusCode())
                .as("training creation must succeed even though the workload broker is unreachable")
                .isEqualTo(201);
    }
}
