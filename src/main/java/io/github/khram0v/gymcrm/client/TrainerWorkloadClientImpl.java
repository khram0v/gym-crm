package io.github.khram0v.gymcrm.client;

import io.github.khram0v.gymcrm.client.dto.WorkloadEventRequest;
import io.github.khram0v.gymcrm.client.security.ServiceTokenProvider;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class TrainerWorkloadClientImpl implements TrainerWorkloadClient {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    private static final String WORKLOAD_URL = "http://trainer-workload-service/api/v1/trainer-workloads";

    private final RestClient restClient;
    private final ServiceTokenProvider serviceTokenProvider;

    public TrainerWorkloadClientImpl(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder builder,
                                     ServiceTokenProvider serviceTokenProvider) {
        this.restClient = builder.build();
        this.serviceTokenProvider = serviceTokenProvider;
    }

    @Override
    @CircuitBreaker(name = "trainerWorkload", fallbackMethod = "onNotifyWorkloadFailure")
    public void notifyWorkload(WorkloadEventRequest request) {
        String transactionId = MDC.get(TRANSACTION_ID);

        restClient.post()
                .uri(WORKLOAD_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceTokenProvider.generateToken())
                .header(TRANSACTION_ID_HEADER, transactionId != null ? transactionId : "")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();

        log.info("Notified trainer-workload-service: {} {} min for trainer '{}' on {}",
                request.actionType(), request.trainingDuration(), request.trainerUsername(), request.trainingDate());
    }

    void onNotifyWorkloadFailure(WorkloadEventRequest request, Throwable throwable) {
        log.error("Failed to notify trainer-workload-service for trainer '{}' (action={}, date={}, duration={}); "
                        + "workload sync skipped: {}",
                request.trainerUsername(), request.actionType(), request.trainingDate(), request.trainingDuration(),
                throwable.getMessage());
    }
}
