package io.github.khram0v.gymcrm.client.config;

import io.github.khram0v.gymcrm.client.security.ServiceTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class RestClientConfigTest {

    @Mock
    private ServiceTokenProvider serviceTokenProvider;

    private MockRestServiceServer mockServer;
    private RestClient restClient;

    @BeforeEach
    void setUp() {
        RestClientConfig config = new RestClientConfig();

        RestClient.Builder builder =
                config.loadBalancedRestClientBuilder(serviceTokenProvider);

        mockServer = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void request_setsExpectedHeaders() {
        when(serviceTokenProvider.generateToken())
                .thenReturn("service-token");

        mockServer.expect(requestTo("/test"))
                .andExpect(method(POST))
                .andExpect(header("Authorization", "Bearer service-token"))
                .andExpect(header("X-Transaction-Id", "transaction-123"))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess());

        MDC.put("transactionId", "transaction-123");

        restClient.post()
                .uri("/test")
                .body("{}")
                .retrieve()
                .toBodilessEntity();

        mockServer.verify();
    }

    @Test
    void request_whenTransactionIdMissing_setsEmptyTransactionHeader() {
        when(serviceTokenProvider.generateToken())
                .thenReturn("service-token");

        mockServer.expect(requestTo("/test"))
                .andExpect(method(POST))
                .andExpect(header("Authorization", "Bearer service-token"))
                .andExpect(header("X-Transaction-Id", ""))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess());

        restClient.post()
                .uri("/test")
                .body("{}")
                .retrieve()
                .toBodilessEntity();

        mockServer.verify();
    }
}
