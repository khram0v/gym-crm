package io.github.khram0v.gymcrm.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.github.khram0v.gymcrm.testsupport.ApiClient;
import io.github.khram0v.gymcrm.testsupport.ScenarioContext;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class AuthSteps {

    private final ApiClient apiClient;
    private final ScenarioContext scenarioContext;

    @Given("a registered trainer")
    public void aRegisteredTrainer() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", "Auth");
        body.put("lastName", "Tester" + System.nanoTime());
        body.put("specializationId", 1);

        HttpResponse<String> registration = apiClient.post("/api/v1/trainers", body, null);
        assertThat(registration.statusCode()).isEqualTo(201);

        JsonNode json = apiClient.json(registration);
        scenarioContext.setTrainerUsername(json.get("username").asString());
        scenarioContext.setTrainerPassword(json.get("password").asString());
    }

    @Given("I have failed to log in {int} times")
    public void iHaveFailedToLogInNTimes(int attempts) {
        for (int i = 0; i < attempts; i++) {
            login(scenarioContext.getTrainerUsername(), "wrong-password-" + i);
        }
    }

    @When("I log in with the correct password")
    public void iLogInWithTheCorrectPassword() {
        HttpResponse<String> loginResponse =
                login(scenarioContext.getTrainerUsername(), scenarioContext.getTrainerPassword());
        scenarioContext.setLastResponse(loginResponse);
        captureTokensIfPresent(loginResponse);
    }

    @When("I log in with password {string}")
    public void iLogInWithPassword(String password) {
        scenarioContext.setLastResponse(login(scenarioContext.getTrainerUsername(), password));
    }

    @When("I log in as unknown user {string} with password {string}")
    public void iLogInAsUnknownUser(String username, String password) {
        scenarioContext.setLastResponse(login(username, password));
    }

    @When("I request my own trainer profile using the access token")
    public void iRequestMyOwnTrainerProfile() {
        scenarioContext.setLastResponse(apiClient.get(
                "/api/v1/trainers/" + scenarioContext.getTrainerUsername(), scenarioContext.getAccessToken()));
    }

    @When("I refresh my session")
    public void iRefreshMySession() {
        String previousRefreshToken = scenarioContext.getRefreshToken();
        Map<String, Object> body = Map.of("refreshToken", previousRefreshToken);
        HttpResponse<String> refreshResponse = apiClient.post("/api/v1/auth/refresh", body, null);
        scenarioContext.setLastResponse(refreshResponse);
        scenarioContext.setPreviousRefreshToken(previousRefreshToken);
        captureTokensIfPresent(refreshResponse);
    }

    @When("I try to refresh again using the previous refresh token")
    public void iTryToRefreshAgainUsingThePreviousRefreshToken() {
        Map<String, Object> body = Map.of("refreshToken", scenarioContext.getPreviousRefreshToken());
        scenarioContext.setLastResponse(apiClient.post("/api/v1/auth/refresh", body, null));
    }

    @When("I log out")
    public void iLogOut() {
        scenarioContext.setLastResponse(
                apiClient.post("/api/v1/auth/logout", Map.of(), scenarioContext.getAccessToken()));
    }

    @When("I log out and also invalidate my refresh token")
    public void iLogOutAndAlsoInvalidateMyRefreshToken() {
        scenarioContext.setPreviousRefreshToken(scenarioContext.getRefreshToken());
        scenarioContext.setLastResponse(apiClient.post(
                "/api/v1/auth/logout", Map.of(),
                scenarioContext.getAccessToken(), scenarioContext.getRefreshToken()));
    }

    private HttpResponse<String> login(String username, String password) {
        Map<String, Object> body = Map.of("username", username, "password", password);
        return apiClient.post("/api/v1/auth/login", body, null);
    }

    private void captureTokensIfPresent(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            return;
        }
        JsonNode json = apiClient.json(response);
        scenarioContext.setAccessToken(json.get("token").asString());
        scenarioContext.setRefreshToken(json.get("refreshToken").asString());
    }
}
