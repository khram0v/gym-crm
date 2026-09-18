package io.github.khram0v.gymcrm.cucumber.steps;

import io.cucumber.java.en.Then;
import io.github.khram0v.gymcrm.testsupport.ApiClient;
import io.github.khram0v.gymcrm.testsupport.ScenarioContext;
import lombok.RequiredArgsConstructor;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class CommonSteps {

    private final ApiClient apiClient;
    private final ScenarioContext scenarioContext;

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertThat(scenarioContext.getLastResponse().statusCode()).isEqualTo(expectedStatus);
    }

    @Then("the response should contain username {string}")
    public void theResponseShouldContainUsername(String expectedUsername) {
        assertThat(apiClient.json(scenarioContext.getLastResponse()).get("username").asString())
                .isEqualTo(expectedUsername);
    }

    @Then("the response should contain a non-blank password")
    public void theResponseShouldContainNonBlankPassword() {
        assertThat(apiClient.json(scenarioContext.getLastResponse()).get("password").asString())
                .isNotBlank();
    }

    @Then("the response should contain a non-blank access token")
    public void theResponseShouldContainANonBlankAccessToken() {
        assertThat(apiClient.json(scenarioContext.getLastResponse()).get("token").asString())
                .isNotBlank();
    }

    @Then("the response should contain a non-blank refresh token")
    public void theResponseShouldContainANonBlankRefreshToken() {
        assertThat(apiClient.json(scenarioContext.getLastResponse()).get("refreshToken").asString())
                .isNotBlank();
    }
}
