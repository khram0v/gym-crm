package io.github.khram0v.gymcrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.khram0v.gymcrm.dto.request.AddTrainingRequest;
import io.github.khram0v.gymcrm.dto.response.TrainingTypeResponse;
import io.github.khram0v.gymcrm.exception.NotFoundException;
import io.github.khram0v.gymcrm.service.AuthService;
import io.github.khram0v.gymcrm.service.TrainingService;
import io.github.khram0v.gymcrm.service.TrainingTypeService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TrainingController.class)
class TrainingControllerTest {

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockitoBean private TrainingService trainingService;
    @MockitoBean private TrainingTypeService trainingTypeService;
    @MockitoBean private AuthService authService;

    private static final String BASIC =
            "Basic " + Base64.getEncoder().encodeToString("u:p".getBytes(StandardCharsets.UTF_8));

    // ~~~~~ addTraining ~~~~~

    @Test
    void addTraining_returns201_andUnpacksArgsInCorrectOrder() throws Exception {
        var request = new AddTrainingRequest(
                "Jane.Smith", "John.Doe", "Cardio Blast",
                LocalDate.of(2024, Month.JUNE, 1), 60);

        mockMvc.perform(post("/api/v1/trainings")
                        .header(HttpHeaders.AUTHORIZATION, BASIC)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(trainingService).addTraining(
                "Jane.Smith", "John.Doe", "Cardio Blast",
                LocalDate.of(2024, Month.JUNE, 1), 60);
    }

    @Test
    void addTraining_whenPartyNotFound_returns404() throws Exception {
        var request = new AddTrainingRequest(
                "Ghost", "John.Doe", "Cardio",
                LocalDate.of(2024, Month.JUNE, 1), 60);
        doThrow(new NotFoundException("Trainer not found: Ghost"))
                .when(trainingService).addTraining(any(), any(), any(), any(), any());

        mockMvc.perform(post("/api/v1/trainings")
                        .header(HttpHeaders.AUTHORIZATION, BASIC)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addTraining_whenBlankTrainingName_returns400_andDoesNotCallService() throws Exception {
        var request = new AddTrainingRequest(
                "Jane.Smith", "John.Doe", "",
                LocalDate.of(2024, Month.JUNE, 1), 60);

        mockMvc.perform(post("/api/v1/trainings")
                        .header(HttpHeaders.AUTHORIZATION, BASIC)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(trainingService, never()).addTraining(any(), any(), any(), any(), any());
    }

    @Test
    void addTraining_whenNonPositiveDuration_returns400() throws Exception {
        var request = new AddTrainingRequest(
                "Jane.Smith", "John.Doe", "Cardio",
                LocalDate.of(2024, Month.JUNE, 1), 0);

        mockMvc.perform(post("/api/v1/trainings")
                        .header(HttpHeaders.AUTHORIZATION, BASIC)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(trainingService, never()).addTraining(any(), any(), any(), any(), any());
    }

    // ~~~~~ getAllTrainingTypes ~~~~~

    @Test
    void getAllTrainingTypes_returns200_andList() throws Exception {
        when(trainingTypeService.getAll()).thenReturn(List.of(
                new TrainingTypeResponse(1L, "Fitness"),
                new TrainingTypeResponse(2L, "Yoga")));

        mockMvc.perform(get("/api/v1/trainings/training-types")
                        .header(HttpHeaders.AUTHORIZATION, BASIC))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Fitness"))
                .andExpect(jsonPath("$[1].name").value("Yoga"));

        verify(trainingTypeService).getAll();
    }
}
