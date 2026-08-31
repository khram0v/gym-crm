package io.github.khram0v.gymcrm.controller;

import io.github.khram0v.gymcrm.dto.request.ActivateRequest;
import io.github.khram0v.gymcrm.dto.request.ChangePasswordRequest;
import io.github.khram0v.gymcrm.dto.request.TrainerRegistrationRequest;
import io.github.khram0v.gymcrm.dto.request.UpdateTrainerRequest;
import io.github.khram0v.gymcrm.dto.response.RegistrationResponse;
import io.github.khram0v.gymcrm.dto.response.TrainerProfileResponse;
import io.github.khram0v.gymcrm.dto.response.TrainerTrainingResponse;
import io.github.khram0v.gymcrm.exception.NotFoundException;
import io.github.khram0v.gymcrm.repository.TrainingRepository;
import io.github.khram0v.gymcrm.security.Role;
import io.github.khram0v.gymcrm.security.jwt.JwtAuthenticationFilter;
import io.github.khram0v.gymcrm.service.TrainerService;
import io.github.khram0v.gymcrm.service.TrainingService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TrainerController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MethodSecurityTestConfig.class)
class TrainerControllerTest {

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @MockitoBean private TrainerService trainerService;
    @MockitoBean private TrainingService trainingService;
    @MockitoBean private TrainingRepository trainingRepository;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    // ~~~~~ register ~~~~~

    @Test
    void register_returns201_unpacksRequest_andBody() throws Exception {
        var request = new TrainerRegistrationRequest("Kate", "Novak", 1L);
        when(trainerService.create("Kate", "Novak", 1L))
                .thenReturn(new RegistrationResponse("Kate.Novak", "pass"));

        mockMvc.perform(post("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("Kate.Novak"))
                .andExpect(jsonPath("$.password").value("pass"));

        verify(trainerService).create("Kate", "Novak", 1L);
    }

    @Test
    void register_whenSpecializationNotFound_returns404() throws Exception {
        var request = new TrainerRegistrationRequest("Kate", "Novak", 99L);
        when(trainerService.create("Kate", "Novak", 99L))
                .thenThrow(new NotFoundException("Training type not found: 99"));

        mockMvc.perform(post("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void register_whenBlankFirstName_returns400_andDoesNotCallService() throws Exception {
        var request = new TrainerRegistrationRequest("", "Novak", 1L);

        mockMvc.perform(post("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(trainerService, never()).create(any(), any(), any());
    }

    // ~~~~~ getProfile ~~~~~

    @Test
    @WithMockUserPrincipal(username = "Jane.Smith", role = Role.TRAINER)
    void getProfile_returns200_andBody() throws Exception {
        when(trainerService.getByUsername("Jane.Smith")).thenReturn(sampleProfile());

        mockMvc.perform(get("/api/v1/trainers/{username}", "Jane.Smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Jane.Smith"))
                .andExpect(jsonPath("$.firstName").value("Jane"));

        verify(trainerService).getByUsername("Jane.Smith");
    }

    @Test
    @WithMockUserPrincipal(username = "Ghost", role = Role.TRAINER)
    void getProfile_whenNotFound_returns404() throws Exception {
        when(trainerService.getByUsername("Ghost"))
                .thenThrow(new NotFoundException("Trainer not found: Ghost"));

        mockMvc.perform(get("/api/v1/trainers/{username}", "Ghost"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUserPrincipal(username = "Someone.Else", role = Role.TRAINER)
    void getProfile_whenNotOwner_returns403_andDoesNotCallService() throws Exception {
        mockMvc.perform(get("/api/v1/trainers/{username}", "Jane.Smith"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(trainerService);
    }

    @Test
    @WithMockUserPrincipal(username = "Jane.Smith", role = Role.TRAINEE)
    void getProfile_whenWrongRole_returns403_andDoesNotCallService() throws Exception {
        mockMvc.perform(get("/api/v1/trainers/{username}", "Jane.Smith"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(trainerService);
    }

    @Test
    @WithMockUserPrincipal(username = "admin", role = Role.ADMIN)
    void getProfile_whenAdmin_returns200_regardlessOfOwnership() throws Exception {
        when(trainerService.getByUsername("Jane.Smith")).thenReturn(sampleProfile());

        mockMvc.perform(get("/api/v1/trainers/{username}", "Jane.Smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Jane.Smith"));

        verify(trainerService).getByUsername("Jane.Smith");
    }

    // ~~~~~ changePassword ~~~~~

    @Test
    @WithMockUserPrincipal(username = "Jane.Smith", role = Role.TRAINER)
    void changePassword_returns200_andUnpacksBody() throws Exception {
        var request = new ChangePasswordRequest("oldPass", "newPass");

        mockMvc.perform(put("/api/v1/trainers/{username}/password", "Jane.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainerService).changePassword("Jane.Smith", "oldPass", "newPass");
    }

    @Test
    @WithMockUserPrincipal(username = "Jane.Smith", role = Role.TRAINER)
    void changePassword_whenBlankNewPassword_returns400() throws Exception {
        var request = new ChangePasswordRequest("oldPass", "");

        mockMvc.perform(put("/api/v1/trainers/{username}/password", "Jane.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(trainerService, never()).changePassword(any(), any(), any());
    }

    @Test
    @WithMockUserPrincipal(username = "Someone.Else", role = Role.TRAINER)
    void changePassword_whenNotOwner_returns403_andDoesNotCallService() throws Exception {
        var request = new ChangePasswordRequest("oldPass", "newPass");

        mockMvc.perform(put("/api/v1/trainers/{username}/password", "Jane.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(trainerService);
    }

    // ~~~~~ updateProfile ~~~~~

    @Test
    @WithMockUserPrincipal(username = "Jane.Smith", role = Role.TRAINER)
    void updateProfile_returns200_unpacksFields_andBody() throws Exception {
        var request = new UpdateTrainerRequest("Janet", "Smith", false);
        when(trainerService.updateProfile("Jane.Smith", "Janet", "Smith", false))
                .thenReturn(sampleProfile());

        mockMvc.perform(put("/api/v1/trainers/{username}", "Jane.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Jane.Smith"));

        verify(trainerService).updateProfile("Jane.Smith", "Janet", "Smith", false);
    }

    @Test
    @WithMockUserPrincipal(username = "Someone.Else", role = Role.TRAINER)
    void updateProfile_whenNotOwner_returns403_andDoesNotCallService() throws Exception {
        var request = new UpdateTrainerRequest("Janet", "Smith", false);

        mockMvc.perform(put("/api/v1/trainers/{username}", "Jane.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(trainerService);
    }

    // ~~~~~ getTrainings ~~~~~

    @Test
    @WithMockUserPrincipal(username = "Jane.Smith", role = Role.TRAINER)
    void getTrainings_passesAllQueryParams_returns200_andList() throws Exception {
        when(trainingService.getTrainerTrainings(
                "Jane.Smith", LocalDate.of(2024, Month.JANUARY, 1), LocalDate.of(2024, Month.DECEMBER, 31),
                "John", "Doe"))
                .thenReturn(List.of(new TrainerTrainingResponse(42L, "Morning Fitness",
                        LocalDate.of(2024, Month.JUNE, 1), "Fitness", 60, "John", "Doe")));

        mockMvc.perform(get("/api/v1/trainers/{username}/trainings", "Jane.Smith")
                        .param("from", "2024-01-01")
                        .param("to", "2024-12-31")
                        .param("traineeFirstName", "John")
                        .param("traineeLastName", "Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value("Morning Fitness"));

        verify(trainingService).getTrainerTrainings(
                "Jane.Smith", LocalDate.of(2024, Month.JANUARY, 1), LocalDate.of(2024, Month.DECEMBER, 31),
                "John", "Doe");
    }

    @Test
    @WithMockUserPrincipal(username = "Jane.Smith", role = Role.TRAINER)
    void getTrainings_withNoParams_passesNulls_returns200() throws Exception {
        when(trainingService.getTrainerTrainings(
                eq("Jane.Smith"), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/trainers/{username}/trainings", "Jane.Smith"))
                .andExpect(status().isOk());

        verify(trainingService).getTrainerTrainings(
                eq("Jane.Smith"), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    @WithMockUserPrincipal(username = "Someone.Else", role = Role.TRAINER)
    void getTrainings_whenNotOwner_returns403_andDoesNotCallService() throws Exception {
        mockMvc.perform(get("/api/v1/trainers/{username}/trainings", "Jane.Smith"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(trainingService);
    }

    // ~~~~~ setActiveStatus ~~~~~

    @Test
    @WithMockUserPrincipal(username = "Jane.Smith", role = Role.TRAINER)
    void setActiveStatus_returns200_andUnpacksActive() throws Exception {
        var request = new ActivateRequest(false);

        mockMvc.perform(patch("/api/v1/trainers/{username}/status", "Jane.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainerService).setActiveStatus("Jane.Smith", false);
    }

    @Test
    @WithMockUserPrincipal(username = "Someone.Else", role = Role.TRAINER)
    void setActiveStatus_whenNotOwner_returns403_andDoesNotCallService() throws Exception {
        var request = new ActivateRequest(false);

        mockMvc.perform(patch("/api/v1/trainers/{username}/status", "Jane.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(trainerService);
    }

    // ~~~~~ helpers ~~~~~

    private TrainerProfileResponse sampleProfile() {
        return new TrainerProfileResponse("Jane.Smith", "Jane", "Smith", null, true, List.of());
    }
}
