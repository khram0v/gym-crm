package io.github.khram0v.gymcrm.security.jwt;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String REFRESH_TOKEN_HEADER = "X-Refresh-Token";

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        blacklistIfPresent(extractBearerToken(request));
        blacklistIfPresent(request.getHeader(REFRESH_TOKEN_HEADER));
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private void blacklistIfPresent(String token) {
        if (token == null) {
            return;
        }

        try {
            Instant expiresAt = jwtService.extractExpiration(token);
            tokenBlacklistService.blacklist(token, expiresAt);
        } catch (JwtException |  IllegalArgumentException e) {
            // token already invalid/expired - nothing to blacklist
        }
    }
}
