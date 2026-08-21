package io.github.khram0v.gymcrm.controller;

import io.github.khram0v.gymcrm.dto.request.LoginRequest;
import io.github.khram0v.gymcrm.dto.request.RefreshRequest;
import io.github.khram0v.gymcrm.exception.AccountLockedException;
import io.github.khram0v.gymcrm.security.CustomUserDetailsService;
import io.github.khram0v.gymcrm.security.Role;
import io.github.khram0v.gymcrm.security.UserPrincipal;
import io.github.khram0v.gymcrm.security.bruteforce.LoginAttemptService;
import io.github.khram0v.gymcrm.security.jwt.JwtService;
import io.github.khram0v.gymcrm.security.jwt.TokenBlacklistService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @MockitoBean private AuthenticationManager authenticationManager;
    @MockitoBean private LoginAttemptService loginAttemptService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private TokenBlacklistService tokenBlacklistService;
    @MockitoBean private CustomUserDetailsService userDetailsService;
    @MockitoBean private MeterRegistry meterRegistry;

    // ~~~~~ login ~~~~~

    @Test
    void login_whenCredentialsValid_returns200_withToken() throws Exception {
        var request = new LoginRequest("John.Doe", "correctPass");
        UserPrincipal principal = new UserPrincipal("John.Doe", "encodedPass", true, Role.TRAINEE);
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken("John.Doe", "TRAINEE")).thenReturn("jwt-token");
        when(jwtService.generateRefreshToken("John.Doe", "TRAINEE")).thenReturn("jwt-refresh-token");
        when(jwtService.getExpirationMs()).thenReturn(3_600_000L);
        stubCounter();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("jwt-refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(3600));

        verify(loginAttemptService).checkNotBlocked("John.Doe");
        verify(loginAttemptService).loginSucceeded("John.Doe");
        verify(loginAttemptService, never()).loginFailed(any());
    }

    @Test
    void login_whenCredentialsInvalid_returns401_andRecordsFailure() throws Exception {
        var request = new LoginRequest("John.Doe", "wrongPass");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        stubCounter();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));

        verify(loginAttemptService).loginFailed("John.Doe");
        verify(loginAttemptService, never()).loginSucceeded(any());
        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void login_whenAccountLocked_returns423_andDoesNotAttemptAuthentication() throws Exception {
        var request = new LoginRequest("John.Doe", "anyPass");

        doThrow(new AccountLockedException("Locked"))
                .when(loginAttemptService).checkNotBlocked("John.Doe");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(423));

        verifyNoInteractions(authenticationManager);
    }

    @Test
    void login_whenBlankUsername_returns400_andDoesNotCallAnything() throws Exception {
        var request = new LoginRequest("", "somePass");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationManager, loginAttemptService);
    }

    // ~~~~~ refresh ~~~~~

    @Test
    void refresh_whenValid_returns200_withRotatedTokens_andBlacklistsOldToken() throws Exception {
        var request = new RefreshRequest("old-refresh-token");
        UserPrincipal principal = new UserPrincipal("John.Doe", "encodedPass", true, Role.TRAINEE);
        Instant expiresAt = Instant.parse("2024-06-01T00:00:00Z");

        when(tokenBlacklistService.isBlacklisted("old-refresh-token")).thenReturn(false);
        when(jwtService.extractUsername("old-refresh-token")).thenReturn("John.Doe");
        when(jwtService.isValidRefreshToken("old-refresh-token", "John.Doe")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("John.Doe")).thenReturn(principal);
        when(jwtService.extractExpiration("old-refresh-token")).thenReturn(expiresAt);
        when(jwtService.generateToken("John.Doe", "TRAINEE")).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken("John.Doe", "TRAINEE")).thenReturn("new-refresh-token");
        when(jwtService.getExpirationMs()).thenReturn(3_600_000L);
        stubCounter();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(3600));

        verify(tokenBlacklistService).blacklist("old-refresh-token", expiresAt);
    }

    @Test
    void refresh_whenTokenBlacklisted_returns401_andDoesNotIssueNewTokens() throws Exception {
        var request = new RefreshRequest("old-refresh-token");
        when(tokenBlacklistService.isBlacklisted("old-refresh-token")).thenReturn(true);
        stubCounter();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired refresh token"));

        verify(jwtService, never()).generateToken(any(), any());
        verify(tokenBlacklistService, never()).blacklist(eq("old-refresh-token"), any());
    }

    @Test
    void refresh_whenTokenIsNotRefreshType_returns401_andDoesNotLoadUser() throws Exception {
        var request = new RefreshRequest("access-token-used-as-refresh");
        when(tokenBlacklistService.isBlacklisted("access-token-used-as-refresh")).thenReturn(false);
        when(jwtService.extractUsername("access-token-used-as-refresh")).thenReturn("John.Doe");
        when(jwtService.isValidRefreshToken("access-token-used-as-refresh", "John.Doe")).thenReturn(false);
        stubCounter();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userDetailsService);
    }

    @Test
    void refresh_whenUserDisabled_returns401_andDoesNotIssueNewTokens() throws Exception {
        var request = new RefreshRequest("refresh-token");
        UserPrincipal disabledPrincipal = new UserPrincipal("John.Doe", "encodedPass", false, Role.TRAINEE);
        when(tokenBlacklistService.isBlacklisted("refresh-token")).thenReturn(false);
        when(jwtService.extractUsername("refresh-token")).thenReturn("John.Doe");
        when(jwtService.isValidRefreshToken("refresh-token", "John.Doe")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("John.Doe")).thenReturn(disabledPrincipal);
        stubCounter();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void refresh_whenUserNoLongerExists_returns401() throws Exception {
        var request = new RefreshRequest("refresh-token");
        when(tokenBlacklistService.isBlacklisted("refresh-token")).thenReturn(false);
        when(jwtService.extractUsername("refresh-token")).thenReturn("Ghost");
        when(jwtService.isValidRefreshToken("refresh-token", "Ghost")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("Ghost"))
                .thenThrow(new UsernameNotFoundException("User not found: Ghost"));
        stubCounter();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_whenBlankRefreshToken_returns400_andDoesNotCallAnything() throws Exception {
        var request = new RefreshRequest("");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(tokenBlacklistService, jwtService, userDetailsService);
    }

    private void stubCounter() {
        Counter counter = mock(Counter.class);
        when(meterRegistry.counter(anyString(), anyString(), anyString())).thenReturn(counter);
    }
}
