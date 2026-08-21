package io.github.khram0v.gymcrm.controller;

import io.github.khram0v.gymcrm.dto.request.AddTrainingRequest;
import io.github.khram0v.gymcrm.dto.response.TrainingTypeResponse;
import io.github.khram0v.gymcrm.exception.ConflictException;
import io.github.khram0v.gymcrm.exception.NotFoundException;
import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.model.Training;
import io.github.khram0v.gymcrm.model.TrainingType;
import io.github.khram0v.gymcrm.repository.TrainingRepository;
import io.github.khram0v.gymcrm.security.Role;
import io.github.khram0v.gymcrm.security.jwt.JwtAuthenticationFilter;
import io.github.khram0v.gymcrm.service.TrainingService;
import io.github.khram0v.gymcrm.service.TrainingTypeService;
import io.github.khram0v.gymcrm.testsupport.security.MethodSecurityTestConfig;
import io.github.khram0v.gymcrm.testsupport.security.WithMockUserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TrainingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MethodSecurityTestConfig.class)
class TrainingControllerTest {

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @MockitoBean private TrainingService trainingService;
    @MockitoBean private TrainingTypeService trainingTypeService;
    @MockitoBean private TrainingRepository trainingRepository;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    // ~~~~~ addTraining ~~~~~

    @Test
    @WithMockUserPrincipal(username = "Jane.Smith", role = Role.TRAINER)
    void addTraining_returns201_andUnpacksArgsInCorrectOrder() throws Exception {
        var request = new AddTrainingRequest(
                "Jane.Smith", "John.Doe", "Cardio Blast",
                LocalDate.of(2024, Month.JUNE, 1), 60);

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(trainingService).addTraining(
                "Jane.Smith", "John.Doe", "Cardio Blast",
                LocalDate.of(2024, Month.JUNE, 1), 60);
    }

    @Test
    @WithMockUserPrincipal(username = "Ghost", role = Role.TRAINER)
    void addTraining_whenPartyNotFound_returns404() throws Exception {
        var request = new AddTrainingRequest(
                "Ghost", "John.Doe", "Cardio",
                LocalDate.of(2024, Month.JUNE, 1), 60);
        doThrow(new NotFoundException("Trainer not found: Ghost"))
                .when(trainingService).addTraining(any(), any(), any(), any(), any());

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUserPrincipal(username = "Jane.Smith", role = Role.TRAINER)
    void addTraining_whenBlankTrainingName_returns400_andDoesNotCallService() throws Exception {
        var request = new AddTrainingRequest(
                "Jane.Smith", "John.Doe", "",
                LocalDate.of(2024, Month.JUNE, 1), 60);

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(trainingService, never()).addTraining(any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUserPrincipal(username = "Jane.Smith", role = Role.TRAINER)
    void addTraining_whenNonPositiveDuration_returns400() throws Exception {
        var request = new AddTrainingRequest(
                "Jane.Smith", "John.Doe", "Cardio",
                LocalDate.of(2024, Month.JUNE, 1), 0);

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(trainingService, never()).addTraining(any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUserPrincipal(username = "Someone.Else", role = Role.TRAINER)
    void addTraining_whenNotOwnerOfTrainerUsername_returns403_andDoesNotCallService() throws Exception {
        var request = new AddTrainingRequest(
                "Jane.Smith", "John.Doe", "Cardio Blast",
                LocalDate.of(2024, Month.JUNE, 1), 60);

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(trainingService);
    }

    @Test
    @WithMockUserPrincipal(username = "Jane.Smith", role = Role.TRAINEE)
    void addTraining_whenWrongRole_returns403_andDoesNotCallService() throws Exception {
        var request = new AddTrainingRequest(
                "Jane.Smith", "John.Doe", "Cardio Blast",
                LocalDate.of(2024, Month.JUNE, 1), 60);

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(trainingService);
    }

    @Test
    @WithMockUserPrincipal(username = "admin", role = Role.ADMIN)
    void addTraining_whenAdmin_returns201_regardlessOfTrainerUsername() throws Exception {
        var request = new AddTrainingRequest(
                "Jane.Smith", "John.Doe", "Cardio Blast",
                LocalDate.of(2024, Month.JUNE, 1), 60);

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(trainingService).addTraining(
                "Jane.Smith", "John.Doe", "Cardio Blast",
                LocalDate.of(2024, Month.JUNE, 1), 60);
    }

    // ~~~~~ deleteTraining ~~~~~

    @Test
    @WithMockUserPrincipal(username = "Jane.Smith", role = Role.TRAINER)
    void deleteTraining_whenOwner_returns204_andCallsService() throws Exception {
        when(trainingRepository.findById(10L)).thenReturn(Optional.of(trainingOwnedBy("Jane.Smith")));

        mockMvc.perform(delete("/api/v1/trainings/{id}", 10L))
                .andExpect(status().isNoContent());

        verify(trainingService).deleteTraining(10L);
    }

    @Test
    @WithMockUserPrincipal(username = "Someone.Else", role = Role.TRAINER)
    void deleteTraining_whenNotOwner_returns403_andDoesNotCallService() throws Exception {
        when(trainingRepository.findById(10L)).thenReturn(Optional.of(trainingOwnedBy("Jane.Smith")));

        mockMvc.perform(delete("/api/v1/trainings/{id}", 10L))
                .andExpect(status().isForbidden());

        verifyNoInteractions(trainingService);
    }

    @Test
    @WithMockUserPrincipal(username = "John.Doe", role = Role.TRAINEE)
    void deleteTraining_whenWrongRole_returns403_andDoesNotCallService() throws Exception {
        mockMvc.perform(delete("/api/v1/trainings/{id}", 10L))
                .andExpect(status().isForbidden());

        verifyNoInteractions(trainingService);
    }

    @Test
    @WithMockUserPrincipal(username = "Jane.Smith", role = Role.TRAINER)
    void deleteTraining_whenTrainingNotFound_returns404FromService() throws Exception {
        when(trainingRepository.findById(99L)).thenReturn(Optional.empty());
        doThrow(new NotFoundException("Training not found: 99"))
                .when(trainingService).deleteTraining(99L);

        mockMvc.perform(delete("/api/v1/trainings/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUserPrincipal(username = "Jane.Smith", role = Role.TRAINER)
    void deleteTraining_whenAlreadyOccurred_returns409FromService() throws Exception {
        when(trainingRepository.findById(10L)).thenReturn(Optional.of(trainingOwnedBy("Jane.Smith")));
        doThrow(new ConflictException("Cannot cancel a training that has already occurred: 10"))
                .when(trainingService).deleteTraining(10L);

        mockMvc.perform(delete("/api/v1/trainings/{id}", 10L))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUserPrincipal(username = "admin", role = Role.ADMIN)
    void deleteTraining_whenAdmin_returns204_regardlessOfOwnership_andSkipsOwnershipLookup() throws Exception {
        mockMvc.perform(delete("/api/v1/trainings/{id}", 10L))
                .andExpect(status().isNoContent());

        verify(trainingService).deleteTraining(10L);
        verifyNoInteractions(trainingRepository);
    }

    // ~~~~~ getAllTrainingTypes ~~~~~

    @Test
    void getAllTrainingTypes_returns200_andList() throws Exception {
        when(trainingTypeService.getAll()).thenReturn(List.of(
                new TrainingTypeResponse(1L, "Fitness"),
                new TrainingTypeResponse(2L, "Yoga")));

        mockMvc.perform(get("/api/v1/trainings/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Fitness"))
                .andExpect(jsonPath("$[1].name").value("Yoga"));

        verify(trainingTypeService).getAll();
    }

    // ~~~~~ helpers ~~~~~

    private Training trainingOwnedBy(String trainerUsername) {
        TrainingType fitness = new TrainingType("Fitness");
        Trainer trainer = new Trainer("Jane", "Smith", fitness);
        trainer.setUsername(trainerUsername);
        Trainee trainee = new Trainee("John", "Doe", null, null);
        trainee.setUsername("John.Doe");
        return new Training(trainee, trainer, "Cardio", fitness, LocalDate.now().plusDays(1), 60);
    }
}
