package io.github.khram0v.gymcrm.cucumber.steps;

import io.cucumber.java.en.Given;
import io.github.khram0v.gymcrm.testsupport.AdminCredentials;
import io.github.khram0v.gymcrm.testsupport.ApiClient;
import io.github.khram0v.gymcrm.testsupport.ScenarioContext;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class IdentitySteps {

    private final ApiClient apiClient;
    private final ScenarioContext scenarioContext;

    @Given("a registered trainee")
    public void aRegisteredTrainee() {
        String[] credentials = registerTrainee("Owner");
        scenarioContext.setTraineeUsername(credentials[0]);
        scenarioContext.setTraineePassword(credentials[1]);
    }

    @Given("a second registered trainee")
    public void aSecondRegisteredTrainee() {
        String[] credentials = registerTrainee("Other");
        scenarioContext.setSecondTraineeUsername(credentials[0]);
        scenarioContext.setSecondTraineePassword(credentials[1]);
    }

    @Given("a second registered trainer")
    public void aSecondRegisteredTrainer() {
        String[] credentials = registerTrainer("Other");
        scenarioContext.setSecondTrainerUsername(credentials[0]);
        scenarioContext.setSecondTrainerPassword(credentials[1]);
    }

    @Given("I am logged in as the trainee")
    public void iAmLoggedInAsTheTrainee() {
        scenarioContext.setAccessToken(
                login(scenarioContext.getTraineeUsername(), scenarioContext.getTraineePassword()));
    }

    @Given("I am logged in as the second trainee")
    public void iAmLoggedInAsTheSecondTrainee() {
        scenarioContext.setAccessToken(
                login(scenarioContext.getSecondTraineeUsername(), scenarioContext.getSecondTraineePassword()));
    }

    @Given("I am logged in as the trainer")
    public void iAmLoggedInAsTheTrainer() {
        scenarioContext.setAccessToken(
                login(scenarioContext.getTrainerUsername(), scenarioContext.getTrainerPassword()));
    }

    @Given("I am logged in as the second trainer")
    public void iAmLoggedInAsTheSecondTrainer() {
        scenarioContext.setAccessToken(
                login(scenarioContext.getSecondTrainerUsername(), scenarioContext.getSecondTrainerPassword()));
    }

    @Given("I am logged in as the admin user")
    public void iAmLoggedInAsTheAdminUser() {
        scenarioContext.setAccessToken(login(AdminCredentials.USERNAME, AdminCredentials.PASSWORD));
    }

    private String[] registerTrainee(String lastNameSuffix) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", "Trainee");
        body.put("lastName", lastNameSuffix + System.nanoTime());

        HttpResponse<String> registration = apiClient.post("/api/v1/trainees", body, null);
        assertThat(registration.statusCode()).isEqualTo(201);
        JsonNode json = apiClient.json(registration);
        return new String[] {json.get("username").asString(), json.get("password").asString()};
    }

    private String[] registerTrainer(String lastNameSuffix) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", "Trainer");
        body.put("lastName", lastNameSuffix + System.nanoTime());
        body.put("specializationId", 1);

        HttpResponse<String> registration = apiClient.post("/api/v1/trainers", body, null);
        assertThat(registration.statusCode()).isEqualTo(201);
        JsonNode json = apiClient.json(registration);
        return new String[] {json.get("username").asString(), json.get("password").asString()};
    }

    private String login(String username, String password) {
        Map<String, Object> body = Map.of("username", username, "password", password);
        HttpResponse<String> response = apiClient.post("/api/v1/auth/login", body, null);
        assertThat(response.statusCode()).isEqualTo(200);
        return apiClient.json(response).get("token").asString();
    }
}
