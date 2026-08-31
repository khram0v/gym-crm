package io.github.khram0v.gymcrm.client;

import io.github.khram0v.gymcrm.client.dto.WorkloadEventRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class TrainerWorkloadClientImpl implements TrainerWorkloadClient {

    private static final String WORKLOAD_URL = "http://trainer-workload-service/api/v1/trainer-workloads";

    private final RestClient restClient;

    public TrainerWorkloadClientImpl(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    @CircuitBreaker(name = "trainerWorkload", fallbackMethod = "onNotifyWorkloadFailure")
    public void notifyWorkload(WorkloadEventRequest request) {
        restClient.post()
                .uri(WORKLOAD_URL)
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
