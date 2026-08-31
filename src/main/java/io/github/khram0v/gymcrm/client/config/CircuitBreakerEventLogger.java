package io.github.khram0v.gymcrm.client.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CircuitBreakerEventLogger {

    private static final String TRAINER_WORKLOAD_CIRCUIT_BREAKER = "trainerWorkload";

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @PostConstruct
    void registerListeners() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(TRAINER_WORKLOAD_CIRCUIT_BREAKER);
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> log.warn(
                        "Circuit breaker '{}' transitioned from {} to {}",
                        TRAINER_WORKLOAD_CIRCUIT_BREAKER,
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()));
    }
}
