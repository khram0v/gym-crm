package io.github.khram0v.gymcrm.interceptor;

import io.github.khram0v.gymcrm.exception.AuthenticationException;
import io.github.khram0v.gymcrm.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock private AuthService authService;

    private AuthInterceptor interceptor;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new AuthInterceptor(authService);
        response = new MockHttpServletResponse();
    }

    private static String basic(String user, String pass) {
        return "Basic " + Base64.getEncoder()
                .encodeToString((user + ":" + pass).getBytes(StandardCharsets.UTF_8));
    }

    private MockHttpServletRequest request(String method, String uri) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod(method);
        req.setRequestURI(uri);
        return req;
    }

    @Test
    void publicEndpoint_traineeRegister_passesWithoutAuth() {
        MockHttpServletRequest req = request("POST", "/api/v1/trainees");

        boolean result = interceptor.preHandle(req, response, new Object());

        assertThat(result).isTrue();
        verifyNoInteractions(authService);
    }

    @Test
    void publicEndpoint_trainerRegister_passesWithoutAuth() {
        boolean result = interceptor.preHandle(
                request("POST", "/api/v1/trainers"), response, new Object());

        assertThat(result).isTrue();
        verifyNoInteractions(authService);
    }

    @Test
    void publicEndpoint_traineeChangePassword_pathVariableMatched_passesWithoutAuth() {
        boolean result = interceptor.preHandle(
                request("PUT", "/api/v1/trainees/John.Doe/password"), response, new Object());

        assertThat(result).isTrue();
        verifyNoInteractions(authService);
    }

    @Test
    void publicEndpoint_trainerChangePassword_pathVariableMatched_passesWithoutAuth() {
        boolean result = interceptor.preHandle(
                request("PUT", "/api/v1/trainers/Jane.Smith/password"), response, new Object());

        assertThat(result).isTrue();
        verifyNoInteractions(authService);
    }

    @Test
    void sameUriDifferentMethod_isNotPublic_requiresAuth() {
        MockHttpServletRequest req = request("GET", "/api/v1/trainees");

        assertThatThrownBy(() -> interceptor.preHandle(req, response, new Object()))
                .isInstanceOf(AuthenticationException.class);
        verifyNoInteractions(authService);
    }

    @Test
    void changePasswordPattern_doesNotMatchProfilePath() {
        MockHttpServletRequest req = request("PUT", "/api/v1/trainees/John.Doe");

        assertThatThrownBy(() -> interceptor.preHandle(req, response, new Object()))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void protectedEndpoint_missingAuthHeader_throws401() {
        MockHttpServletRequest req = request("GET", "/api/v1/trainees/John.Doe");

        assertThatThrownBy(() -> interceptor.preHandle(req, response, new Object()))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Missing or invalid Authorization header");
        verifyNoInteractions(authService);
    }

    @Test
    void protectedEndpoint_nonBasicScheme_throws401() {
        MockHttpServletRequest req = request("GET", "/api/v1/trainees/John.Doe");
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer sometoken");

        assertThatThrownBy(() -> interceptor.preHandle(req, response, new Object()))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Missing or invalid Authorization header");
        verifyNoInteractions(authService);
    }

    @Test
    void protectedEndpoint_validBasic_decodesAndDelegates_thenPasses() {
        MockHttpServletRequest req = request("GET", "/api/v1/trainees/John.Doe");
        req.addHeader(HttpHeaders.AUTHORIZATION, basic("John.Doe", "secret"));

        boolean result = interceptor.preHandle(req, response, new Object());

        assertThat(result).isTrue();
        verify(authService).authenticate("John.Doe", "secret");
    }

    @Test
    void validBasic_passwordContainsColon_splitsOnFirstColonOnly() {
        MockHttpServletRequest req = request("GET", "/api/v1/trainees/John.Doe");
        req.addHeader(HttpHeaders.AUTHORIZATION, basic("John.Doe", "pa:ss:word"));

        interceptor.preHandle(req, response, new Object());

        verify(authService).authenticate("John.Doe", "pa:ss:word");
    }

    @Test
    void protectedEndpoint_noColonInDecoded_throwsMalformed() {
        MockHttpServletRequest req = request("GET", "/api/v1/trainees/John.Doe");
        String noColon = Base64.getEncoder()
                .encodeToString("nocolonhere".getBytes(StandardCharsets.UTF_8));
        req.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + noColon);

        assertThatThrownBy(() -> interceptor.preHandle(req, response, new Object()))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Malformed Basic credentials");
        verifyNoInteractions(authService);
    }

    @Test
    void protectedEndpoint_invalidBase64_throwsMalformed() {
        MockHttpServletRequest req = request("GET", "/api/v1/trainees/John.Doe");
        req.addHeader(HttpHeaders.AUTHORIZATION, "Basic !!!not-base64!!!");

        assertThatThrownBy(() -> interceptor.preHandle(req, response, new Object()))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Malformed Basic credentials");
        verifyNoInteractions(authService);
    }

    @Test
    void protectedEndpoint_authServiceThrows_propagates() {
        MockHttpServletRequest req = request("GET", "/api/v1/trainees/John.Doe");
        req.addHeader(HttpHeaders.AUTHORIZATION, basic("John.Doe", "wrong"));
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authService).authenticate("John.Doe", "wrong");

        assertThatThrownBy(() -> interceptor.preHandle(req, response, new Object()))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid credentials");
    }
}
