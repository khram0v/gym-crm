package io.github.khram0v.gymcrm.cucumber.steps;

import io.cucumber.java.en.When;
import io.github.khram0v.gymcrm.testsupport.ApiClient;
import io.github.khram0v.gymcrm.testsupport.ScenarioContext;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
public class TrainerProfileSteps {

    private final ApiClient apiClient;
    private final ScenarioContext scenarioContext;

    @When("I request the trainer profile for the registered trainer")
    public void requestTrainerProfileForRegisteredTrainer() {
        requestProfile(scenarioContext.getTrainerUsername());
    }

    @When("I request the trainer profile for {string}")
    public void requestTrainerProfileFor(String username) {
        requestProfile(username);
    }

    @When("I update the trainer profile for the registered trainer with first name {string}")
    public void updateTrainerProfile(String firstName) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", "Smith");
        body.put("active", true);

        scenarioContext.setLastResponse(apiClient.put(
                "/api/v1/trainers/" + scenarioContext.getTrainerUsername(), body, scenarioContext.getAccessToken()));
    }

    @When("I set the active status of the registered trainer to {word}")
    public void setActiveStatusOfRegisteredTrainer(String active) {
        Map<String, Object> body = Map.of("active", Boolean.parseBoolean(active));
        scenarioContext.setLastResponse(apiClient.patch(
                "/api/v1/trainers/" + scenarioContext.getTrainerUsername() + "/status",
                body, scenarioContext.getAccessToken()));
    }

    private void requestProfile(String username) {
        scenarioContext.setLastResponse(
                apiClient.get("/api/v1/trainers/" + username, scenarioContext.getAccessToken()));
    }
}
