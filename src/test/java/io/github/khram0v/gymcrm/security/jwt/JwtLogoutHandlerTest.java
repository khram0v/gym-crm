package io.github.khram0v.gymcrm.security.jwt;

import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtLogoutHandlerTest {

    private static final String REFRESH_TOKEN_HEADER = "X-Refresh-Token";

    @Mock private JwtService jwtService;
    @Mock private TokenBlacklistService tokenBlacklistService;

    @InjectMocks private JwtLogoutHandler logoutHandler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void logout_whenNoAuthorizationHeader_doesNothing() {
        logoutHandler.logout(request, response, null);

        verifyNoInteractions(jwtService, tokenBlacklistService);
    }

    @Test
    void logout_whenHeaderNotBearer_doesNothing() {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic abc123");

        logoutHandler.logout(request, response, null);

        verifyNoInteractions(jwtService, tokenBlacklistService);
    }

    @Test
    void logout_whenValidBearerToken_blacklistsWithExpiration() {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token123");
        Instant expiresAt = Instant.parse("2024-06-01T00:00:00Z");
        when(jwtService.extractExpiration("token123")).thenReturn(expiresAt);

        logoutHandler.logout(request, response, null);

        verify(tokenBlacklistService).blacklist("token123", expiresAt);
    }

    @Test
    void logout_whenTokenAlreadyInvalid_doesNotPropagate_andDoesNotBlacklist() {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer bad-token");
        when(jwtService.extractExpiration("bad-token")).thenThrow(new MalformedJwtException("bad token"));

        assertThatCode(() -> logoutHandler.logout(request, response, null))
                .doesNotThrowAnyException();

        verifyNoInteractions(tokenBlacklistService);
    }

    @Test
    void logout_whenRefreshTokenHeaderPresent_blacklistsItToo() {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer access-token");
        request.addHeader(REFRESH_TOKEN_HEADER, "refresh-token");
        Instant accessExpiresAt = Instant.parse("2024-06-01T00:00:00Z");
        Instant refreshExpiresAt = Instant.parse("2024-06-08T00:00:00Z");
        when(jwtService.extractExpiration("access-token")).thenReturn(accessExpiresAt);
        when(jwtService.extractExpiration("refresh-token")).thenReturn(refreshExpiresAt);

        logoutHandler.logout(request, response, null);

        verify(tokenBlacklistService).blacklist("access-token", accessExpiresAt);
        verify(tokenBlacklistService).blacklist("refresh-token", refreshExpiresAt);
    }

    @Test
    void logout_whenOnlyRefreshTokenHeaderPresent_blacklistsOnlyThat() {
        request.addHeader(REFRESH_TOKEN_HEADER, "refresh-token");
        Instant refreshExpiresAt = Instant.parse("2024-06-08T00:00:00Z");
        when(jwtService.extractExpiration("refresh-token")).thenReturn(refreshExpiresAt);

        logoutHandler.logout(request, response, null);

        verify(tokenBlacklistService).blacklist("refresh-token", refreshExpiresAt);
        verify(tokenBlacklistService, times(1)).blacklist(any(), any());
    }

    @Test
    void logout_whenRefreshTokenHeaderInvalid_doesNotPropagate_andDoesNotBlacklistIt() {
        request.addHeader(REFRESH_TOKEN_HEADER, "bad-refresh-token");
        when(jwtService.extractExpiration("bad-refresh-token"))
                .thenThrow(new MalformedJwtException("bad token"));

        assertThatCode(() -> logoutHandler.logout(request, response, null))
                .doesNotThrowAnyException();

        verifyNoInteractions(tokenBlacklistService);
    }
}
