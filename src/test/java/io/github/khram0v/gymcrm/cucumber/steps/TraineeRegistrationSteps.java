package io.github.khram0v.gymcrm.cucumber.steps;

import io.cucumber.java.en.When;
import io.github.khram0v.gymcrm.testsupport.ApiClient;
import io.github.khram0v.gymcrm.testsupport.ScenarioContext;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
public class TraineeRegistrationSteps {

    private final ApiClient apiClient;
    private final ScenarioContext scenarioContext;

    @When("a new trainee registers with first name {string} and last name {string}")
    public void registerTrainee(String firstName, String lastName) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);

        scenarioContext.setLastResponse(apiClient.post("/api/v1/trainees", body, null));
    }
}
