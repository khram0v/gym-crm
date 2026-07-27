package io.github.khram0v.gymcrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.khram0v.gymcrm.dto.request.ActivateRequest;
import io.github.khram0v.gymcrm.dto.request.ChangePasswordRequest;
import io.github.khram0v.gymcrm.dto.request.TraineeRegistrationRequest;
import io.github.khram0v.gymcrm.dto.request.UpdateTraineeRequest;
import io.github.khram0v.gymcrm.dto.request.UpdateTraineeTrainersRequest;
import io.github.khram0v.gymcrm.dto.response.RegistrationResponse;
import io.github.khram0v.gymcrm.dto.response.TraineeProfileResponse;
import io.github.khram0v.gymcrm.dto.response.TraineeTrainingResponse;
import io.github.khram0v.gymcrm.dto.response.TrainerSummary;
import io.github.khram0v.gymcrm.exception.NotFoundException;
import io.github.khram0v.gymcrm.service.AuthService;
import io.github.khram0v.gymcrm.service.TraineeService;
import io.github.khram0v.gymcrm.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Month;
import java.util.Base64;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TraineeController.class)
class TraineeControllerTest {

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockitoBean private TraineeService traineeService;
    @MockitoBean private TrainingService trainingService;
    @MockitoBean private AuthService authService;

    private static final String BASIC =
            "Basic " + Base64.getEncoder().encodeToString("u:p".getBytes(StandardCharsets.UTF_8));

    // ~~~~~ register ~~~~~

    @Test
    void register_returns201_unpacksRequest_andReturnsBody() throws Exception {
        var request = new TraineeRegistrationRequest("Alan", "Poe",
                LocalDate.of(2000, Month.JANUARY, 1), "Main St");
        when(traineeService.create("Alan", "Poe", LocalDate.of(2000, Month.JANUARY, 1), "Main St"))
                .thenReturn(new RegistrationResponse("Alan.Poe", "pass123"));

        mockMvc.perform(post("/api/v1/trainees")
                        .header(HttpHeaders.AUTHORIZATION, BASIC)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("Alan.Poe"))
                .andExpect(jsonPath("$.password").value("pass123"));

        verify(traineeService).create("Alan", "Poe", LocalDate.of(2000, Month.JANUARY, 1), "Main St");
    }

    @Test
    void register_whenBlankFirstName_returns400_andDoesNotCallService() throws Exception {
        var request = new TraineeRegistrationRequest("", "Poe",
                LocalDate.of(2000, Month.JANUARY, 1), "Main St");

        mockMvc.perform(post("/api/v1/trainees")
                        .header(HttpHeaders.AUTHORIZATION, BASIC)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(traineeService, never()).create(any(), any(), any(), any());
    }

    // ~~~~~ getProfile ~~~~~

    @Test
    void getProfile_returns200_andBody() throws Exception {
        when(traineeService.getByUsername("John.Doe")).thenReturn(sampleProfile());

        mockMvc.perform(get("/api/v1/trainees/{username}", "John.Doe")
                        .header(HttpHeaders.AUTHORIZATION, BASIC))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("John.Doe"))
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(traineeService).getByUsername("John.Doe");
    }

    @Test
    void getProfile_whenNotFound_returns404() throws Exception {
        when(traineeService.getByUsername("Ghost"))
                .thenThrow(new NotFoundException("Trainee not found: Ghost"));

        mockMvc.perform(get("/api/v1/trainees/{username}", "Ghost")
                        .header(HttpHeaders.AUTHORIZATION, BASIC))
                .andExpect(status().isNotFound());
    }

    // ~~~~~ changePassword ~~~~~

    @Test
    void changePassword_returns200_andUnpacksBody() throws Exception {
        var request = new ChangePasswordRequest("oldPass", "newPass");

        mockMvc.perform(put("/api/v1/trainees/{username}/password", "John.Doe")
                        .header(HttpHeaders.AUTHORIZATION, BASIC)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(traineeService).changePassword("John.Doe", "oldPass", "newPass");
    }

    @Test
    void changePassword_whenBlankNewPassword_returns400() throws Exception {
        var request = new ChangePasswordRequest("oldPass", "");

        mockMvc.perform(put("/api/v1/trainees/{username}/password", "John.Doe")
                        .header(HttpHeaders.AUTHORIZATION, BASIC)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(traineeService, never()).changePassword(any(), any(), any());
    }

    // ~~~~~ updateProfile ~~~~~

    @Test
    void updateProfile_returns200_unpacksAllFields_andBody() throws Exception {
        var request = new UpdateTraineeRequest("Johnny", "Doe",
                LocalDate.of(1999, Month.MAY, 5), "New Address", false);
        when(traineeService.updateProfile("John.Doe", "Johnny", "Doe",
                LocalDate.of(1999, Month.MAY, 5), "New Address", false))
                .thenReturn(sampleProfile());

        mockMvc.perform(put("/api/v1/trainees/{username}", "John.Doe")
                        .header(HttpHeaders.AUTHORIZATION, BASIC)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("John.Doe"));

        verify(traineeService).updateProfile("John.Doe", "Johnny", "Doe",
                LocalDate.of(1999, Month.MAY, 5), "New Address", false);
    }

    // ~~~~~ delete ~~~~~

    @Test
    void delete_returns204_andCallsService() throws Exception {
        mockMvc.perform(delete("/api/v1/trainees/{username}", "John.Doe")
                        .header(HttpHeaders.AUTHORIZATION, BASIC))
                .andExpect(status().isNoContent());

        verify(traineeService).deleteByUsername("John.Doe");
    }

    // ~~~~~ getUnassignedTrainers ~~~~~

    @Test
    void getUnassignedTrainers_returns200_andList() throws Exception {
        when(traineeService.getUnassignedTrainers("John.Doe"))
                .thenReturn(List.of(new TrainerSummary("Ann.Lee", "Ann", "Lee", null)));

        mockMvc.perform(get("/api/v1/trainees/{username}/unassigned-trainers", "John.Doe")
                        .header(HttpHeaders.AUTHORIZATION, BASIC))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("Ann.Lee"));

        verify(traineeService).getUnassignedTrainers("John.Doe");
    }

    // ~~~~~ updateTrainers ~~~~~

    @Test
    void updateTrainers_returns200_unpacksUsernames_andList() throws Exception {
        var request = new UpdateTraineeTrainersRequest(List.of("Ann.Lee", "Bob.Fox"));
        when(traineeService.updateTrainers("John.Doe", List.of("Ann.Lee", "Bob.Fox")))
                .thenReturn(List.of(new TrainerSummary("Ann.Lee", "Ann", "Lee", null)));

        mockMvc.perform(put("/api/v1/trainees/{username}/trainers", "John.Doe")
                        .header(HttpHeaders.AUTHORIZATION, BASIC)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("Ann.Lee"));

        verify(traineeService).updateTrainers("John.Doe", List.of("Ann.Lee", "Bob.Fox"));
    }

    // ~~~~~ getTrainings ~~~~~

    @Test
    void getTrainings_passesAllQueryParams_returns200_andList() throws Exception {
        when(trainingService.getTraineeTrainings(
                "John.Doe", LocalDate.of(2024, Month.JANUARY, 1), LocalDate.of(2024, Month.DECEMBER, 31),
                "Jane", "Smith", "Fitness"))
                .thenReturn(List.of(new TraineeTrainingResponse("Morning Fitness",
                        LocalDate.of(2024, Month.JUNE, 1), "Fitness", 60, "Jane", "Smith")));

        mockMvc.perform(get("/api/v1/trainees/{username}/trainings", "John.Doe")
                        .header(HttpHeaders.AUTHORIZATION, BASIC)
                        .param("from", "2024-01-01")
                        .param("to", "2024-12-31")
                        .param("trainerFirstName", "Jane")
                        .param("trainerLastName", "Smith")
                        .param("trainingType", "Fitness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value("Morning Fitness"));

        verify(trainingService).getTraineeTrainings(
                "John.Doe", LocalDate.of(2024, Month.JANUARY, 1), LocalDate.of(2024, Month.DECEMBER, 31),
                "Jane", "Smith", "Fitness");
    }

    @Test
    void getTrainings_withNoParams_passesNulls_returns200() throws Exception {
        when(trainingService.getTraineeTrainings(
                eq("John.Doe"), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/trainees/{username}/trainings", "John.Doe")
                        .header(HttpHeaders.AUTHORIZATION, BASIC))
                .andExpect(status().isOk());

        verify(trainingService).getTraineeTrainings(
                eq("John.Doe"), isNull(), isNull(), isNull(), isNull(), isNull());
    }

    // ~~~~~ setActiveStatus ~~~~~

    @Test
    void setActiveStatus_returns200_andUnpacksActive() throws Exception {
        var request = new ActivateRequest(false);

        mockMvc.perform(patch("/api/v1/trainees/{username}/status", "John.Doe")
                        .header(HttpHeaders.AUTHORIZATION, BASIC)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(traineeService).setActiveStatus("John.Doe", false);
    }

    // ~~~~~ helpers ~~~~~

    private TraineeProfileResponse sampleProfile() {
        return new TraineeProfileResponse("John.Doe", "John", "Doe",
                LocalDate.of(1995, Month.MAY, 20), "123 Main St", true, List.of());
    }
}
