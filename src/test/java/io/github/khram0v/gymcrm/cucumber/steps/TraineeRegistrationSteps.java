package io.github.khram0v.gymcrm.cucumber.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class TraineeRegistrationSteps {

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;

    private HttpResponse<String> response;

    @When("a new trainee registers with first name {string} and last name {string}")
    public void registerTrainee(String firstName, String lastName) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/trainees"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertThat(response.statusCode()).isEqualTo(expectedStatus);
    }

    @Then("the response should contain username {string}")
    public void theResponseShouldContainUsername(String expectedUsername) {
        JsonNode body = objectMapper.readTree(response.body());
        assertThat(body.get("username").asString()).isEqualTo(expectedUsername);
    }

    @Then("the response should contain a non-blank password")
    public void theResponseShouldContainNonBlankPassword() {
        JsonNode body = objectMapper.readTree(response.body());
        assertThat(body.get("password").asString()).isNotBlank();
    }
}
