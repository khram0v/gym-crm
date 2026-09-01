package io.github.khram0v.gymcrm.client;

import io.github.khram0v.gymcrm.client.dto.WorkloadEventRequest;
import io.github.khram0v.gymcrm.client.messaging.MessagingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class TrainerWorkloadClientImpl implements TrainerWorkloadClient {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    private final MessagingProperties messagingProperties;

    @Override
    public void notifyWorkload(WorkloadEventRequest request) {
        try {
            String payload = objectMapper.writeValueAsString(request);

            jmsTemplate.send(messagingProperties.trainerWorkloadEventsQueue(),
                    session -> session.createTextMessage(payload));

            log.info("Published workload event to queue '{}': {} {} min for trainer '{}' on {}",
                    messagingProperties.trainerWorkloadEventsQueue(), request.actionType(),
                    request.trainingDuration(), request.trainerUsername(), request.trainingDate());
        } catch (RuntimeException e) {
            log.error("Failed to publish workload event for trainer '{}' (action={}, date={}, duration={}); "
                    + "workload sync skipped: {}",
                    request.trainerUsername(), request.actionType(), request.trainingDate(),
                    request.trainingDuration(), e.getMessage());
        }
    }
}
