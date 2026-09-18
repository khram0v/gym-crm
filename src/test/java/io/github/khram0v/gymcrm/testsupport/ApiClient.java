package io.github.khram0v.gymcrm.testsupport;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RequiredArgsConstructor
public class ApiClient {

    private final int port;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public HttpResponse<String> get(String path, String bearerToken) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(uri(path)).GET();
        if (bearerToken != null) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }
        return send(builder);
    }

    public HttpResponse<String> post(String path, Object body, String bearerToken) {
        return post(path, body, bearerToken, null);
    }

    public HttpResponse<String> post(String path, Object body, String bearerToken, String refreshTokenHeader) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri(path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
        if (bearerToken != null) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }
        if (refreshTokenHeader != null) {
            builder.header("X-Refresh-Token", refreshTokenHeader);
        }
        return send(builder);
    }

    public HttpResponse<String> delete(String path, String bearerToken) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(uri(path)).DELETE();
        if (bearerToken != null) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }
        return send(builder);
    }

    public JsonNode json(HttpResponse<String> response) {
        return objectMapper.readTree(response.body());
    }

    private HttpResponse<String> send(HttpRequest.Builder builder) {
        try {
            return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("HTTP call failed in test", e);
        }
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
