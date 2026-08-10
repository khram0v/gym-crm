package io.github.khram0v.gymcrm.dto.response;

public record RefreshResponse(
        String token,
        String refreshToken,
        String tokenType,
        long expiresInSeconds
) {
}
