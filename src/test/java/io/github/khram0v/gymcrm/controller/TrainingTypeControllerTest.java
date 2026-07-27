package io.github.khram0v.gymcrm.controller;

import io.github.khram0v.gymcrm.dto.response.TrainingTypeResponse;
import io.github.khram0v.gymcrm.service.AuthService;
import io.github.khram0v.gymcrm.service.TrainingTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TrainingTypeController.class)
class TrainingTypeControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private TrainingTypeService trainingTypeService;
    @MockitoBean private AuthService authService;

    private static final String BASIC =
            "Basic " + Base64.getEncoder().encodeToString("u:p".getBytes(StandardCharsets.UTF_8));

    @Test
    void getAll_returns200_andList() throws Exception {
        when(trainingTypeService.getAll()).thenReturn(List.of(
                new TrainingTypeResponse(1L, "Fitness"),
                new TrainingTypeResponse(2L, "Yoga")));

        mockMvc.perform(get("/api/v1/training-types")
                        .header(HttpHeaders.AUTHORIZATION, BASIC))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Fitness"))
                .andExpect(jsonPath("$[1].name").value("Yoga"));

        verify(trainingTypeService).getAll();
    }
}
