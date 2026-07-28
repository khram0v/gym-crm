package io.github.khram0v.gymcrm.interceptor;

import io.github.khram0v.gymcrm.exception.AuthenticationException;
import io.github.khram0v.gymcrm.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private record PublicEndpoint(String method, String pattern) {}

    private static final List<PublicEndpoint> PUBLIC_ENDPOINTS = List.of(
            new PublicEndpoint("POST", "/api/v1/trainees"),
            new PublicEndpoint("POST", "/api/v1/trainers"),
            new PublicEndpoint("PUT",  "/api/v1/trainees/*/password"),
            new PublicEndpoint("PUT",  "/api/v1/trainers/*/password")
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (isPublic(request)) {
            return true;
        }
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Basic ")) {
            throw new AuthenticationException("Missing or invalid Authorization header");
        }
        String[] credentials = decode(header.substring("Basic ".length()));
        authService.authenticate(credentials[0], credentials[1]);
        return true;
    }

    private boolean isPublic(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(e -> e.method().equals(method) && pathMatcher.match(e.pattern(), uri));
    }

    private String[] decode(String base64) {
        try {
            String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
            int sep = decoded.indexOf(':');
            if (sep < 0) {
                throw new AuthenticationException("Malformed Basic credentials");
            }
            return new String[]{decoded.substring(0, sep), decoded.substring(sep + 1)};
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException("Malformed Basic credentials");
        }
    }
}
