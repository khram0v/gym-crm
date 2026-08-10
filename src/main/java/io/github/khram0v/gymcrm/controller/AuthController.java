package io.github.khram0v.gymcrm.controller;

import io.github.khram0v.gymcrm.api.AuthApi;
import io.github.khram0v.gymcrm.dto.request.LoginRequest;
import io.github.khram0v.gymcrm.dto.response.LoginResponse;
import io.github.khram0v.gymcrm.exception.AuthenticationException;
import io.github.khram0v.gymcrm.security.UserPrincipal;
import io.github.khram0v.gymcrm.security.bruteforce.LoginAttemptService;
import io.github.khram0v.gymcrm.security.jwt.JwtService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;
    private final JwtService jwtService;
    private final MeterRegistry meterRegistry;

    @Override
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        loginAttemptService.checkNotBlocked(request.username());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            String token = jwtService.generateToken(principal.getUsername(), principal.getRole().name());
            String refreshToken = jwtService.generateRefreshToken(principal.getUsername(), principal.getRole().name());

            loginAttemptService.loginSucceeded(request.username());
            meterRegistry.counter("gym.auth.attempts", "result", "success").increment();

            return new LoginResponse(token, refreshToken, "Bearer", jwtService.getExpirationMs() / 1000);
        } catch (org.springframework.security.core.AuthenticationException e) {
            loginAttemptService.loginFailed(request.username());
            meterRegistry.counter("gym.auth.attempts", "result", "failure").increment();
            throw new AuthenticationException("Invalid username or password");
        }
    }
}
