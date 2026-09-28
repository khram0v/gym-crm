package io.github.khram0v.gymcrm.cucumber.steps;

import io.cucumber.java.en.When;
import io.github.khram0v.gymcrm.testsupport.ApiClient;
import io.github.khram0v.gymcrm.testsupport.ScenarioContext;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
public class TraineeProfileSteps {

    private final ApiClient apiClient;
    private final ScenarioContext scenarioContext;

    @When("I request the trainee profile for the registered trainee")
    public void requestTraineeProfileForRegisteredTrainee() {
        requestProfile(scenarioContext.getTraineeUsername());
    }

    @When("I request the trainee profile for {string}")
    public void requestTraineeProfileFor(String username) {
        requestProfile(username);
    }

    @When("I update the trainee profile for the registered trainee with first name {string}")
    public void updateTraineeProfile(String firstName) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", "Doe");
        body.put("dateOfBirth", null);
        body.put("address", null);
        body.put("active", true);

        scenarioContext.setLastResponse(apiClient.put(
                "/api/v1/trainees/" + scenarioContext.getTraineeUsername(), body, scenarioContext.getAccessToken()));
    }

    @When("I set the active status of the registered trainee to {word}")
    public void setActiveStatusOfRegisteredTrainee(String active) {
        Map<String, Object> body = Map.of("active", Boolean.parseBoolean(active));
        scenarioContext.setLastResponse(apiClient.patch(
                "/api/v1/trainees/" + scenarioContext.getTraineeUsername() + "/status",
                body, scenarioContext.getAccessToken()));
    }

    @When("I delete the registered trainee profile")
    public void deleteRegisteredTraineeProfile() {
        scenarioContext.setLastResponse(apiClient.delete(
                "/api/v1/trainees/" + scenarioContext.getTraineeUsername(), scenarioContext.getAccessToken()));
    }

    private void requestProfile(String username) {
        scenarioContext.setLastResponse(
                apiClient.get("/api/v1/trainees/" + username, scenarioContext.getAccessToken()));
    }
}
