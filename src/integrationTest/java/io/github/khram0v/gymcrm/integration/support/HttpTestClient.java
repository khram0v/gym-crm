package io.github.khram0v.gymcrm.integration.support;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpTestClient {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

    private final String baseUri;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public HttpTestClient(String baseUri) {
        this.baseUri = baseUri;
    }

    public HttpResponse<String> get(String path, String bearerToken) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(uri(path)).GET();
        applyAuth(builder, bearerToken);
        return send(builder);
    }

    public HttpResponse<String> post(String path, Object body, String bearerToken) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri(path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(body)));
        applyAuth(builder, bearerToken);
        return send(builder);
    }

    public HttpResponse<String> delete(String path, String bearerToken) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(uri(path)).DELETE();
        applyAuth(builder, bearerToken);
        return send(builder);
    }

    public JsonNode json(HttpResponse<String> response) {
        return OBJECT_MAPPER.readTree(response.body());
    }

    private void applyAuth(HttpRequest.Builder builder, String bearerToken) {
        if (bearerToken != null) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }
    }

    private HttpResponse<String> send(HttpRequest.Builder builder) {
        try {
            return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("HTTP call failed in integration test", e);
        }
    }

    private URI uri(String path) {
        return URI.create(baseUri + path);
    }
}
