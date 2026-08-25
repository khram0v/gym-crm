package io.github.khram0v.gymcrm.client;

import io.github.khram0v.gymcrm.client.dto.ActionType;
import io.github.khram0v.gymcrm.client.dto.WorkloadEventRequest;
import io.github.khram0v.gymcrm.client.security.ServiceTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadClientImplTest {

    @Mock private ServiceTokenProvider serviceTokenProvider;

    private MockRestServiceServer mockServer;
    private TrainerWorkloadClientImpl client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new TrainerWorkloadClientImpl(builder, serviceTokenProvider);
    }

    @Test
    void notifyWorkload_sendsExpectedRequest_withBearerTokenAndTransactionHeader() {
        when(serviceTokenProvider.generateToken()).thenReturn("service-token");
        mockServer.expect(requestTo("http://trainer-workload-service/api/v1/trainer-workloads"))
                .andExpect(method(POST))
                .andExpect(header("Authorization", "Bearer service-token"))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header("X-Transaction-Id", ""))
                .andRespond(withSuccess());

        WorkloadEventRequest request = new WorkloadEventRequest(
                "Jane.Smith", "Jane", "Smith", true,
                LocalDate.of(2024, Month.JUNE, 10), 60, ActionType.ADD);

        assertThatCode(() -> client.notifyWorkload(request)).doesNotThrowAnyException();

        mockServer.verify();
    }

    @Test
    void notifyWorkload_whenServerReturnsError_propagatesException_forCircuitBreakerToHandle() {
        when(serviceTokenProvider.generateToken()).thenReturn("service-token");
        mockServer.expect(requestTo("http://trainer-workload-service/api/v1/trainer-workloads"))
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

        assertThatCode(() -> client.onNotifyWorkloadFailure(request, new RuntimeException("boom")))
                .doesNotThrowAnyException();
    }
}
