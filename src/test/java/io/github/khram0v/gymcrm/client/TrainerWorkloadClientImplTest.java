package io.github.khram0v.gymcrm.client;

import io.github.khram0v.gymcrm.client.dto.ActionType;
import io.github.khram0v.gymcrm.client.dto.WorkloadEventRequest;
import io.github.khram0v.gymcrm.client.messaging.MessagingProperties;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.UncategorizedJmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadClientImplTest {

    @Mock private JmsTemplate jmsTemplate;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();
    private final MessagingProperties messagingProperties = new MessagingProperties("trainer-workload.events");

    private TrainerWorkloadClientImpl client;

    @BeforeEach
    void setUp() {
        client = new TrainerWorkloadClientImpl(jmsTemplate, objectMapper, messagingProperties);
    }

    private WorkloadEventRequest sampleRequest() {
        return new WorkloadEventRequest(
                "Jane.Smith", "Jane", "Smith", true,
                LocalDate.of(2024, Month.JUNE, 10), 60, ActionType.ADD);
    }

    @Test
    void notifyWorkload_sendsToConfiguredQueue() {
        client.notifyWorkload(sampleRequest());

        verify(jmsTemplate).send(eq("trainer-workload.events"), any(MessageCreator.class));
    }

    @Test
    void notifyWorkload_messageCreatorProducesJsonTextMessageMatchingRequest() throws JMSException {
        WorkloadEventRequest request = sampleRequest();
        client.notifyWorkload(request);

        ArgumentCaptor<MessageCreator> creatorCaptor = ArgumentCaptor.forClass(MessageCreator.class);
        verify(jmsTemplate).send(eq("trainer-workload.events"), creatorCaptor.capture());

        Session session = mock(Session.class);
        TextMessage textMessage = mock(TextMessage.class);
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        when(session.createTextMessage(jsonCaptor.capture())).thenReturn(textMessage);

        creatorCaptor.getValue().createMessage(session);

        WorkloadEventRequest parsed = objectMapper.readValue(jsonCaptor.getValue(), WorkloadEventRequest.class);
        assertThat(parsed).isEqualTo(request);
    }

    @Test
    void notifyWorkload_whenJmsSendFails_logsAndDoesNotPropagate() {
        doThrow(new UncategorizedJmsException("boom"))
                .when(jmsTemplate).send(any(String.class), any(MessageCreator.class));

        assertThatCode(() -> client.notifyWorkload(sampleRequest())).doesNotThrowAnyException();
    }

    @Test
    void notifyWorkload_whenSerializationFails_logsAndDoesNotPropagate_andNeverCallsJmsTemplate() {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsString(any())).thenThrow(new RuntimeException("boom"));
        TrainerWorkloadClientImpl failingClient =
                new TrainerWorkloadClientImpl(jmsTemplate, failingMapper, messagingProperties);

        assertThatCode(() -> failingClient.notifyWorkload(sampleRequest())).doesNotThrowAnyException();

        verifyNoInteractions(jmsTemplate);
    }
}
