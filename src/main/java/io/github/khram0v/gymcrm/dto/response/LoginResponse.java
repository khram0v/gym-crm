package io.github.khram0v.gymcrm.dto.response;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresInSeconds
) {
}
