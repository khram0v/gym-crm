package io.github.khram0v.gymcrm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityResponseWriterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private SecurityResponseWriter writer;

    @BeforeEach
    void setUp() {
        writer = new SecurityResponseWriter(objectMapper);
    }

    @Test
    void write_setsStatusContentTypeAndJsonBody() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/trainees/John.Doe");
        MockHttpServletResponse response = new MockHttpServletResponse();

        writer.write(request, response, HttpStatus.UNAUTHORIZED, "Missing or invalid authentication token");

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(body.get("status").asInt()).isEqualTo(401);
        assertThat(body.get("error").asString()).isEqualTo("Unauthorized");
        assertThat(body.get("message").asString()).isEqualTo("Missing or invalid authentication token");
        assertThat(body.get("path").asString()).isEqualTo("/api/v1/trainees/John.Doe");
    }

    @Test
    void write_whenTransactionIdInMdc_includesItInBody() throws IOException {
        MDC.put("transactionId", "tx-123");
        try {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            writer.write(request, response, HttpStatus.FORBIDDEN, "Access denied");

            JsonNode body = objectMapper.readTree(response.getContentAsString());
            assertThat(body.get("transactionId").asString()).isEqualTo("tx-123");
        } finally {
            MDC.remove("transactionId");
        }
    }
}
