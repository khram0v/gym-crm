package io.github.khram0v.gymcrm.integration.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.khram0v.gymcrm.integration.support.HttpTestClient;
import io.github.khram0v.gymcrm.integration.support.ServiceJwtIssuer;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class CrossServiceSteps {

    @LocalServerPort
    private int gymCrmPort;

    private HttpTestClient gymCrmClient;
    private HttpTestClient workloadServiceClient;

    private String trainerUsername;
    private String traineeUsername;
    private String trainerAccessToken;
    private Long trainingId;
    private HttpResponse<String> lastResponse;

    private HttpTestClient gymCrm() {
        if (gymCrmClient == null) {
            gymCrmClient = new HttpTestClient("http://localhost:" + gymCrmPort);
        }
        return gymCrmClient;
    }

    private HttpTestClient workloadService() {
        if (workloadServiceClient == null) {
            int port = io.github.khram0v.gymcrm.integration.cucumber.CucumberSpringConfiguration
                    .trainerWorkloadServicePort();
            workloadServiceClient = new HttpTestClient("http://localhost:" + port);
        }
        return workloadServiceClient;
    }

    @Given("a registered trainer and trainee in gym-crm")
    public void aRegisteredTrainerAndTraineeInGymCrm() {
        Map<String, Object> trainerBody = new LinkedHashMap<>();
        trainerBody.put("firstName", "Integration");
        trainerBody.put("lastName", "Trainer" + System.nanoTime());
        trainerBody.put("specializationId", 1);
        HttpResponse<String> trainerResponse = gymCrm().post("/api/v1/trainers", trainerBody, null);
        assertThat(trainerResponse.statusCode()).isEqualTo(201);
        JsonNode trainerJson = gymCrm().json(trainerResponse);
        trainerUsername = trainerJson.get("username").asString();
        String trainerPassword = trainerJson.get("password").asString();

        Map<String, Object> traineeBody = new LinkedHashMap<>();
        traineeBody.put("firstName", "Integration");
        traineeBody.put("lastName", "Trainee" + System.nanoTime());
        HttpResponse<String> traineeResponse = gymCrm().post("/api/v1/trainees", traineeBody, null);
        assertThat(traineeResponse.statusCode()).isEqualTo(201);
        traineeUsername = gymCrm().json(traineeResponse).get("username").asString();

        Map<String, Object> loginBody = Map.of("username", trainerUsername, "password", trainerPassword);
        HttpResponse<String> loginResponse = gymCrm().post("/api/v1/auth/login", loginBody, null);
        assertThat(loginResponse.statusCode()).isEqualTo(200);
        trainerAccessToken = gymCrm().json(loginResponse).get("token").asString();
    }

    @When("gym-crm adds a training of {int} minutes on {string} for them")
    public void gymCrmAddsATraining(int duration, String isoDate) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("trainerUsername", trainerUsername);
        body.put("traineeUsername", traineeUsername);
        body.put("trainingName", "Integration Session");
        body.put("trainingDate", isoDate);
        body.put("trainingDuration", duration);

        lastResponse = gymCrm().post("/api/v1/trainings", body, trainerAccessToken);
    }

    @Then("gym-crm's response status should be {int}")
    public void gymCrmsResponseStatusShouldBe(int expectedStatus) {
        assertThat(lastResponse.statusCode()).isEqualTo(expectedStatus);
    }

    @Then("trainer-workload-service eventually reports {int} minutes for that trainer in {int}-{int}")
    public void trainerWorkloadServiceEventuallyReports(int expectedDuration, int year, int month) {
        String token = ServiceJwtIssuer.serviceToken("gym-crm-integration-test");
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            HttpResponse<String> response = workloadService().get(
                    "/api/v1/trainer-workloads/" + trainerUsername + "/years/" + year + "/months/" + month, token);
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(workloadService().json(response).get("trainingSummaryDuration").asInt())
                    .isEqualTo(expectedDuration);
        });
    }

    @Given("a future training exists for them scheduled on {string}")
    public void aFutureTrainingExists(String isoDate) {
        gymCrmAddsATraining(60, isoDate);
        assertThat(lastResponse.statusCode()).isEqualTo(201);

        HttpResponse<String> listResponse = gymCrm().get(
                "/api/v1/trainers/" + trainerUsername + "/trainings", trainerAccessToken);
        assertThat(listResponse.statusCode()).isEqualTo(200);

        trainingId = null;
        for (JsonNode training : gymCrm().json(listResponse)) {
            if (training.get("trainingDate").asString().equals(isoDate)) {
                trainingId = training.get("id").asLong();
            }
        }
        assertThat(trainingId).as("newly added training should appear in the trainer's trainings list").isNotNull();
    }

    @When("gym-crm cancels that training")
    public void gymCrmCancelsThatTraining() {
        lastResponse = gymCrm().delete("/api/v1/trainings/" + trainingId, trainerAccessToken);
    }
}
