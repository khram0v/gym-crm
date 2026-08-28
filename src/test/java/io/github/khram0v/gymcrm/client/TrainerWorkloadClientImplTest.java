package io.github.khram0v.gymcrm.client;

import io.github.khram0v.gymcrm.client.dto.ActionType;
import io.github.khram0v.gymcrm.client.dto.WorkloadEventRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadClientImplTest {

    private MockRestServiceServer mockServer;
    private TrainerWorkloadClientImpl client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new TrainerWorkloadClientImpl(builder);
    }

    @Test
    void notifyWorkload_sendsExpectedRequest() {
        mockServer.expect(requestTo(
                        "http://trainer-workload-service/api/v1/trainer-workloads"))
                .andExpect(method(POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess());

        WorkloadEventRequest request = new WorkloadEventRequest(
                "Jane.Smith", "Jane", "Smith", true,
                LocalDate.of(2024, Month.JUNE, 10), 60, ActionType.ADD);

        assertThatCode(() -> client.notifyWorkload(request))
                .doesNotThrowAnyException();

        mockServer.verify();
    }

    @Test
    void notifyWorkload_whenServerReturnsError_propagatesException() {
        mockServer.expect(requestTo(
                        "http://trainer-workload-service/api/v1/trainer-workloads"))
                .andExpect(method(POST))
                .andRespond(withServerError());

        WorkloadEventRequest request = new WorkloadEventRequest(
                "Jane.Smith", "Jane", "Smith", true,
                LocalDate.of(2024, Month.JUNE, 10), 60, ActionType.ADD);

        assertThatThrownBy(() -> client.notifyWorkload(request))
                .isInstanceOf(RestClientResponseException.class);
    }

    @Test
    void onNotifyWorkloadFailure_logsAndDoesNotThrow() {
        WorkloadEventRequest request = new WorkloadEventRequest(
                "Jane.Smith", "Jane", "Smith", true,
                LocalDate.of(2024, Month.JUNE, 10), 60, ActionType.ADD);

        assertThatCode(() ->
                client.onNotifyWorkloadFailure(request, new RuntimeException("boom")))
                .doesNotThrowAnyException();
    }
}
