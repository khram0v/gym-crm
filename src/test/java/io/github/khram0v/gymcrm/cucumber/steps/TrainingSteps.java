package io.github.khram0v.gymcrm.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.khram0v.gymcrm.client.TrainerWorkloadClient;
import io.github.khram0v.gymcrm.client.dto.ActionType;
import io.github.khram0v.gymcrm.client.dto.WorkloadEventRequest;
import io.github.khram0v.gymcrm.testsupport.ApiClient;
import io.github.khram0v.gymcrm.testsupport.ScenarioContext;
import lombok.RequiredArgsConstructor;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RequiredArgsConstructor
public class TrainingSteps {

    private final ApiClient apiClient;
    private final ScenarioContext scenarioContext;
    private final TrainerWorkloadClient trainerWorkloadClient;

    @When("I add a training for the registered trainer and trainee scheduled tomorrow")
    public void addTrainingForRegisteredTomorrow() {
        addTraining(scenarioContext.getTrainerUsername(), scenarioContext.getTraineeUsername(),
                "Cardio Session", LocalDate.now().plusDays(1), 60);
    }

    @When("I add a training for trainer {string} and the registered trainee scheduled tomorrow")
    public void addTrainingForUnknownTrainer(String trainerUsername) {
        addTraining(trainerUsername, scenarioContext.getTraineeUsername(),
                "Cardio Session", LocalDate.now().plusDays(1), 60);
    }

    @When("I add a training for the registered trainer and trainee {string} scheduled tomorrow")
    public void addTrainingForUnknownTrainee(String traineeUsername) {
        addTraining(scenarioContext.getTrainerUsername(), traineeUsername,
                "Cardio Session", LocalDate.now().plusDays(1), 60);
    }

    @When("I add a training with a blank name for the registered trainer and trainee")
    public void addTrainingWithBlankName() {
        addTraining(scenarioContext.getTrainerUsername(), scenarioContext.getTraineeUsername(),
                "", LocalDate.now().plusDays(1), 60);
    }

    @When("I add a training with a non-positive duration for the registered trainer and trainee")
    public void addTrainingWithNonPositiveDuration() {
        addTraining(scenarioContext.getTrainerUsername(), scenarioContext.getTraineeUsername(),
                "Cardio Session", LocalDate.now().plusDays(1), 0);
    }

    @Given("a future training exists for the registered trainer and trainee")
    public void aFutureTrainingExists() {
        createTrainingAsOwnerAndCaptureId(LocalDate.now().plusDays(1));
    }

    @Given("a past training exists for the registered trainer and trainee")
    public void aPastTrainingExists() {
        createTrainingAsOwnerAndCaptureId(LocalDate.now().minusDays(1));
    }

    @When("I cancel that training")
    public void cancelThatTraining() {
        cancelTraining(scenarioContext.getTrainingId());
    }

    @When("I cancel training {string}")
    public void cancelTrainingById(String id) {
        cancelTraining(Long.parseLong(id));
    }

    @Then("the workload service is notified of an ADD event for the registered trainer")
    public void theWorkloadServiceIsNotifiedOfAnAddEvent() {
        verifyWorkloadNotification(ActionType.ADD);
    }

    @Then("the workload service is notified of a DELETE event for the registered trainer")
    public void theWorkloadServiceIsNotifiedOfADeleteEvent() {
        verifyWorkloadNotification(ActionType.DELETE);
    }

    @Then("the workload service is not notified")
    public void theWorkloadServiceIsNotNotified() {
        verify(trainerWorkloadClient, never()).notifyWorkload(any(WorkloadEventRequest.class));
    }

    private void verifyWorkloadNotification(ActionType actionType) {
        ArgumentCaptor<WorkloadEventRequest> captor = ArgumentCaptor.forClass(WorkloadEventRequest.class);
        verify(trainerWorkloadClient).notifyWorkload(captor.capture());
        assertThat(captor.getValue().trainerUsername()).isEqualTo(scenarioContext.getTrainerUsername());
        assertThat(captor.getValue().actionType()).isEqualTo(actionType);
    }

    private void createTrainingAsOwnerAndCaptureId(LocalDate date) {
        String trainerToken = login(scenarioContext.getTrainerUsername(), scenarioContext.getTrainerPassword());
        String trainingName = "Cardio Session " + System.nanoTime();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("trainerUsername", scenarioContext.getTrainerUsername());
        body.put("traineeUsername", scenarioContext.getTraineeUsername());
        body.put("trainingName", trainingName);
        body.put("trainingDate", date.toString());
        body.put("trainingDuration", 60);

        HttpResponse<String> addResponse = apiClient.post("/api/v1/trainings", body, trainerToken);
        assertThat(addResponse.statusCode()).isEqualTo(201);

        HttpResponse<String> listResponse = apiClient.get(
                "/api/v1/trainers/" + scenarioContext.getTrainerUsername() + "/trainings", trainerToken);
        assertThat(listResponse.statusCode()).isEqualTo(200);

        Long id = null;
        for (JsonNode training : apiClient.json(listResponse)) {
            if (training.get("trainingName").asString().equals(trainingName)) {
                id = training.get("id").asLong();
                break;
            }
        }
        assertThat(id).as("created training should be findable in the trainer's trainings list").isNotNull();
        scenarioContext.setTrainingId(id);

        Mockito.reset(trainerWorkloadClient);
    }

    private void addTraining(String trainerUsername, String traineeUsername,
                             String trainingName, LocalDate date, int duration) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("trainerUsername", trainerUsername);
        body.put("traineeUsername", traineeUsername);
        body.put("trainingName", trainingName);
        body.put("trainingDate", date.toString());
        body.put("trainingDuration", duration);

        scenarioContext.setLastResponse(apiClient.post("/api/v1/trainings", body, scenarioContext.getAccessToken()));
    }

    private void cancelTraining(Long id) {
        scenarioContext.setLastResponse(
                apiClient.delete("/api/v1/trainings/" + id, scenarioContext.getAccessToken()));
    }

    private String login(String username, String password) {
        Map<String, Object> loginBody = Map.of("username", username, "password", password);
        HttpResponse<String> response = apiClient.post("/api/v1/auth/login", loginBody, null);
        assertThat(response.statusCode()).isEqualTo(200);
        return apiClient.json(response).get("token").asString();
    }
}
